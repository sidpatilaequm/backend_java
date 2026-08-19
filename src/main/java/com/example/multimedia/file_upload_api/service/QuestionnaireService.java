package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.questionnaire.*;
import com.example.multimedia.file_upload_api.repository.SupplierRegistrationRepository;
import com.example.multimedia.file_upload_api.repository.questionnaire.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reads/writes Form Studio's own questionnaire tables (processes/sections/questions/
 * question_options/responses/answers/answer_options) directly via JPA — same shared database,
 * same tables Form Studio's FastAPI (dynamic_questions) owns for admin authoring. This keeps the
 * seller-facing Become-a-Supplier path independent of Form Studio's uptime: only the admin
 * builder UI depends on Form Studio being reachable (via QuestionnaireProxyController), not
 * registration submission.
 *
 * Writing into the exact `responses` table Form Studio's own edit-lock check
 * (crud.count_responses) queries means that lock fires correctly for questions answered here,
 * with no extra coordination needed between the two services.
 */
@Service
public class QuestionnaireService {

    @Value("${questionnaire.external-key.supplier-registration:become_a_supplier}")
    private String supplierRegistrationKey;

    private final QSProcessRepository processRepository;
    private final QSSectionRepository sectionRepository;
    private final QSQuestionRepository questionRepository;
    private final QSQuestionOptionRepository optionRepository;
    private final QSQuestionColumnRepository columnRepository;
    private final QSResponseRepository responseRepository;
    private final QSAnswerRepository answerRepository;
    private final QSAnswerOptionRepository answerOptionRepository;
    private final SupplierRegistrationRepository registrationRepository;

    public QuestionnaireService(QSProcessRepository processRepository,
                                 QSSectionRepository sectionRepository,
                                 QSQuestionRepository questionRepository,
                                 QSQuestionOptionRepository optionRepository,
                                 QSQuestionColumnRepository columnRepository,
                                 QSResponseRepository responseRepository,
                                 QSAnswerRepository answerRepository,
                                 QSAnswerOptionRepository answerOptionRepository,
                                 SupplierRegistrationRepository registrationRepository) {
        this.processRepository = processRepository;
        this.sectionRepository = sectionRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.columnRepository = columnRepository;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
        this.answerOptionRepository = answerOptionRepository;
        this.registrationRepository = registrationRepository;
    }

    /**
     * How many applicants have a draft in progress against this specific questionnaire — Form
     * Studio's own response-count-based lock has no visibility into this at all, since a draft
     * only becomes a real `responses` row at submit time (see validateAndPersistAnswers). Used
     * both to show "N drafts in progress" in the admin builder and to block deleting a
     * questionnaire out from under someone who's mid-form.
     */
    public long countDraftsForProcess(Integer processId) {
        return registrationRepository.countByStatusAndDynamicQuestionnaireProcessId("DRAFT", processId);
    }

    /**
     * total: every question in the questionnaire. unanswered: how many have no answer yet
     * (mandatory or not). unansweredRequired: the subset of those that are actually mandatory.
     * incompleteSectionNames: names of sections with at least one unanswered question, in
     * section order — for the draft-saved email's "Still to complete" row, so an applicant sees
     * which parts of the form to revisit without reading a full list of question prompts.
     */
    public record QuestionnaireProgress(int total, int unanswered, int unansweredRequired, List<String> incompleteSectionNames) {}

    /**
     * Progress against the active questionnaire — for the draft-saved email's "Answered X of Y" /
     * "Required left" / "Still to complete" rows, not a submit-time gate (see
     * validateAndPersistAnswers for that). A plain presence check, not a full re-validation
     * (bounds/option-membership don't matter for "is it blank").
     */
    public QuestionnaireProgress countQuestionnaireProgress(String answersJson, Integer processId) {
        if (processId == null) return new QuestionnaireProgress(0, 0, 0, List.of());
        List<QSSection> sections = sectionRepository.findByProcessIdOrderByPosition(processId);
        List<Integer> sectionIds = sections.stream().map(QSSection::getId).collect(Collectors.toList());
        List<QSQuestion> questions = sectionIds.isEmpty() ? List.of() : questionRepository.findBySectionIdInOrderByPosition(sectionIds);
        Map<Integer, String> sectionNameById = sections.stream()
                .collect(Collectors.toMap(QSSection::getId, QSSection::getTitle));

        JSONArray answersArr = new JSONArray(Optional.ofNullable(answersJson).filter(s -> !s.isBlank()).orElse("[]"));
        Map<Integer, JSONObject> byQuestionId = new HashMap<>();
        for (int i = 0; i < answersArr.length(); i++) {
            JSONObject a = answersArr.getJSONObject(i);
            byQuestionId.put(a.getInt("questionId"), a);
        }

        int unanswered = 0;
        int unansweredRequired = 0;
        LinkedHashSet<String> incompleteSections = new LinkedHashSet<>();
        for (QSQuestion q : questions) {
            JSONObject answer = byQuestionId.get(q.getId());
            boolean answered;
            if ("short_text".equals(q.getQuestionType()) || "counter".equals(q.getQuestionType())) {
                answered = answer != null && !answer.optString("textValue", "").isBlank();
            } else if ("table".equals(q.getQuestionType())) {
                answered = answer != null && hasFilledRow(answer.optJSONArray("rows"));
            } else {
                answered = answer != null && answer.optJSONArray("optionIds") != null && answer.getJSONArray("optionIds").length() > 0;
            }
            if (!answered) {
                unanswered++;
                if (Boolean.TRUE.equals(q.getIsMandatory())) unansweredRequired++;
                String sectionName = sectionNameById.get(q.getSectionId());
                if (sectionName != null) incompleteSections.add(sectionName);
            }
        }
        return new QuestionnaireProgress(questions.size(), unanswered, unansweredRequired, new ArrayList<>(incompleteSections));
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) { super(message); }
    }

    /** Nested {process, sections: [{..., questions: [{..., options: [...]}]}]} for the seller-facing page. Null if none is published+active. */
    public JSONObject getActiveQuestionnaire() {
        QSProcess process = processRepository.findByExternalKeyAndStatus(supplierRegistrationKey, "published").orElse(null);
        if (process == null) return null;
        return serializeProcess(process);
    }

    private JSONObject serializeProcess(QSProcess process) {
        List<QSSection> sections = sectionRepository.findByProcessIdOrderByPosition(process.getId());
        List<Integer> sectionIds = sections.stream().map(QSSection::getId).collect(Collectors.toList());
        List<QSQuestion> questions = sectionIds.isEmpty() ? List.of() : questionRepository.findBySectionIdInOrderByPosition(sectionIds);
        Map<Integer, List<QSQuestion>> bySection = questions.stream().collect(Collectors.groupingBy(QSQuestion::getSectionId, LinkedHashMap::new, Collectors.toList()));
        List<Integer> questionIds = questions.stream().map(QSQuestion::getId).collect(Collectors.toList());
        List<QSQuestionOption> options = questionIds.isEmpty() ? List.of() : optionRepository.findByQuestionIdInOrderByPosition(questionIds);
        Map<Integer, List<QSQuestionOption>> byQuestion = options.stream().collect(Collectors.groupingBy(QSQuestionOption::getQuestionId, LinkedHashMap::new, Collectors.toList()));
        List<QSQuestionColumn> columns = questionIds.isEmpty() ? List.of() : columnRepository.findByQuestionIdInOrderByPosition(questionIds);
        Map<Integer, List<QSQuestionColumn>> columnsByQuestion = columns.stream().collect(Collectors.groupingBy(QSQuestionColumn::getQuestionId, LinkedHashMap::new, Collectors.toList()));

        JSONObject out = new JSONObject();
        out.put("processId", process.getId());
        out.put("name", process.getName());
        JSONArray sectionsOut = new JSONArray();
        for (QSSection s : sections) {
            JSONObject sOut = new JSONObject();
            sOut.put("sectionId", s.getId());
            sOut.put("title", s.getTitle());
            JSONArray questionsOut = new JSONArray();
            for (QSQuestion q : bySection.getOrDefault(s.getId(), List.of())) {
                JSONObject qOut = new JSONObject();
                qOut.put("questionId", q.getId());
                qOut.put("prompt", q.getPrompt());
                qOut.put("helpText", q.getHelpText());
                qOut.put("questionType", q.getQuestionType());
                qOut.put("isMandatory", Boolean.TRUE.equals(q.getIsMandatory()));
                qOut.put("maxLength", q.getMaxLength());
                qOut.put("minSelections", q.getMinSelections());
                qOut.put("maxSelections", q.getMaxSelections());
                qOut.put("isDropdown", Boolean.TRUE.equals(q.getIsDropdown()));
                qOut.put("minValue", q.getMinValue());
                qOut.put("maxValue", q.getMaxValue());
                qOut.put("minRows", q.getMinRows());
                qOut.put("maxRows", q.getMaxRows());
                JSONArray optsOut = new JSONArray();
                for (QSQuestionOption o : byQuestion.getOrDefault(q.getId(), List.of())) {
                    JSONObject oOut = new JSONObject();
                    oOut.put("optionId", o.getId());
                    oOut.put("label", o.getLabel());
                    optsOut.put(oOut);
                }
                qOut.put("options", optsOut);
                JSONArray colsOut = new JSONArray();
                for (QSQuestionColumn c : columnsByQuestion.getOrDefault(q.getId(), List.of())) {
                    JSONObject cOut = new JSONObject();
                    cOut.put("columnId", c.getId());
                    cOut.put("label", c.getLabel());
                    cOut.put("columnType", c.getColumnType());
                    cOut.put("isRequired", Boolean.TRUE.equals(c.getIsRequired()));
                    colsOut.put(cOut);
                }
                qOut.put("columns", colsOut);
                questionsOut.put(qOut);
            }
            sOut.put("questions", questionsOut);
            sectionsOut.put(sOut);
        }
        out.put("sections", sectionsOut);
        return out;
    }

    /**
     * Validates {@code answersJson} (array of {questionId, textValue?, optionIds?}) against the
     * active questionnaire's question definitions, then persists a QSResponse/QSAnswer(s)/
     * QSAnswerOption(s) and returns the new response id. Throws ValidationException (message
     * matches the "Missing required: ..." style the rest of submit() already uses) if a
     * mandatory question was left unanswered or a choice answer doesn't fit its question's rules.
     * No-op (returns null) if there is no active questionnaire — dynamic questions are optional
     * unless an admin has actually published and activated one.
     *
     * Deliberately NOT @Transactional here — this is only ever called from submit(), which
     * already is. A separate @Transactional boundary on this method marks the shared transaction
     * rollback-only the instant ValidationException is thrown here, even though submit() catches
     * it and tries to return a clean error response — the eventual commit then fails with a
     * generic UnexpectedRollbackException instead of surfacing the real validation message.
     * Safe to skip: every write below happens only after validation fully passes, so there's
     * never a partial write here that would need its own rollback boundary.
     */
    public Integer validateAndPersistAnswers(String answersJson, String respondentName, String respondentEmail) {
        QSProcess process = processRepository.findByExternalKeyAndStatus(supplierRegistrationKey, "published").orElse(null);
        if (process == null) return null;

        List<QSSection> sections = sectionRepository.findByProcessIdOrderByPosition(process.getId());
        List<Integer> sectionIds = sections.stream().map(QSSection::getId).collect(Collectors.toList());
        List<QSQuestion> questions = sectionIds.isEmpty() ? List.of() : questionRepository.findBySectionIdInOrderByPosition(sectionIds);
        if (questions.isEmpty()) return null;

        List<Integer> questionIds = questions.stream().map(QSQuestion::getId).collect(Collectors.toList());
        List<QSQuestionOption> allOptions = optionRepository.findByQuestionIdInOrderByPosition(questionIds);
        Map<Integer, Set<Integer>> validOptionIdsByQuestion = allOptions.stream()
                .collect(Collectors.groupingBy(QSQuestionOption::getQuestionId, Collectors.mapping(QSQuestionOption::getId, Collectors.toSet())));
        List<QSQuestionColumn> allColumns = columnRepository.findByQuestionIdInOrderByPosition(questionIds);
        Map<Integer, List<QSQuestionColumn>> columnsByQuestion = allColumns.stream()
                .collect(Collectors.groupingBy(QSQuestionColumn::getQuestionId, LinkedHashMap::new, Collectors.toList()));

        JSONArray answersArr = new JSONArray(Optional.ofNullable(answersJson).filter(s -> !s.isBlank()).orElse("[]"));
        Map<Integer, JSONObject> byQuestionId = new HashMap<>();
        for (int i = 0; i < answersArr.length(); i++) {
            JSONObject a = answersArr.getJSONObject(i);
            byQuestionId.put(a.getInt("questionId"), a);
        }

        for (QSQuestion q : questions) {
            JSONObject answer = byQuestionId.get(q.getId());
            String type = q.getQuestionType();
            boolean mandatory = Boolean.TRUE.equals(q.getIsMandatory());

            if ("short_text".equals(type)) {
                String text = answer != null ? answer.optString("textValue", "").trim() : "";
                if (mandatory && text.isEmpty()) {
                    throw new ValidationException("\"" + q.getPrompt() + "\" is required.");
                }
                continue;
            }

            if ("counter".equals(type)) {
                String text = answer != null ? answer.optString("textValue", "").trim() : "";
                if (text.isEmpty()) {
                    if (mandatory) throw new ValidationException("\"" + q.getPrompt() + "\" is required.");
                    continue;
                }
                int value;
                try {
                    value = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    throw new ValidationException("\"" + q.getPrompt() + "\" needs a whole number.");
                }
                if (q.getMinValue() != null && value < q.getMinValue()) {
                    throw new ValidationException("\"" + q.getPrompt() + "\" must be at least " + q.getMinValue() + ".");
                }
                if (q.getMaxValue() != null && value > q.getMaxValue()) {
                    throw new ValidationException("\"" + q.getPrompt() + "\" must be at most " + q.getMaxValue() + ".");
                }
                continue;
            }

            if ("table".equals(type)) {
                List<QSQuestionColumn> cols = columnsByQuestion.getOrDefault(q.getId(), List.of());
                List<JSONObject> rows = filledRows(answer != null ? answer.optJSONArray("rows") : null);

                if (rows.isEmpty()) {
                    if (mandatory || q.getMinRows() != null) {
                        int floor = q.getMinRows() != null ? q.getMinRows() : 1;
                        throw new ValidationException("\"" + q.getPrompt() + "\" needs at least " + floor + " row" + (floor == 1 ? "" : "s") + ".");
                    }
                    continue;
                }
                if (q.getMinRows() != null && rows.size() < q.getMinRows()) {
                    throw new ValidationException("\"" + q.getPrompt() + "\" needs at least " + q.getMinRows() + " rows.");
                }
                if (q.getMaxRows() != null && rows.size() > q.getMaxRows()) {
                    throw new ValidationException("\"" + q.getPrompt() + "\" allows at most " + q.getMaxRows() + " rows.");
                }
                for (int r = 0; r < rows.size(); r++) {
                    JSONObject row = rows.get(r);
                    for (QSQuestionColumn c : cols) {
                        String cell = row.optString(String.valueOf(c.getId()), "").trim();
                        if (Boolean.TRUE.equals(c.getIsRequired()) && cell.isEmpty()) {
                            throw new ValidationException("\"" + q.getPrompt() + "\" row " + (r + 1) + " is missing " + c.getLabel() + ".");
                        }
                        if (!cell.isEmpty() && !cellIsValid(c.getColumnType(), cell)) {
                            String kind = "number".equals(c.getColumnType()) ? "number" : "date";
                            throw new ValidationException("\"" + q.getPrompt() + "\" row " + (r + 1) + ": " + c.getLabel() + " needs a " + kind + ".");
                        }
                    }
                }
                continue;
            }

            List<Integer> optionIds = answer != null && answer.has("optionIds")
                    ? toIntList(answer.getJSONArray("optionIds"))
                    : List.of();
            if (optionIds.isEmpty()) {
                if (mandatory) throw new ValidationException("\"" + q.getPrompt() + "\" is required.");
                continue;
            }

            Set<Integer> valid = validOptionIdsByQuestion.getOrDefault(q.getId(), Set.of());
            if (!valid.containsAll(optionIds)) {
                throw new ValidationException("\"" + q.getPrompt() + "\" has an option that no longer exists — please re-select.");
            }
            if ("single_choice".equals(type) && optionIds.size() != 1) {
                throw new ValidationException("\"" + q.getPrompt() + "\" needs exactly one answer.");
            }
            if ("multi_choice".equals(type)) {
                if (q.getMinSelections() != null && optionIds.size() < q.getMinSelections()) {
                    throw new ValidationException("\"" + q.getPrompt() + "\" needs at least " + q.getMinSelections() + " selected.");
                }
                if (q.getMaxSelections() != null && optionIds.size() > q.getMaxSelections()) {
                    throw new ValidationException("\"" + q.getPrompt() + "\" allows at most " + q.getMaxSelections() + " selected.");
                }
            }
        }

        QSResponse response = new QSResponse();
        response.setProcessId(process.getId());
        response.setRespondentName(respondentName);
        response.setRespondentEmail(respondentEmail);
        response.setSubmittedAt(LocalDateTime.now());
        response = responseRepository.save(response);

        for (QSQuestion q : questions) {
            JSONObject answer = byQuestionId.get(q.getId());
            if (answer == null) continue;

            String qType = q.getQuestionType();
            if ("short_text".equals(qType) || "counter".equals(qType)) {
                String text = answer.optString("textValue", "").trim();
                if (text.isEmpty()) continue;
                QSAnswer a = new QSAnswer();
                a.setResponseId(response.getId());
                a.setQuestionId(q.getId());
                a.setTextValue(text);
                answerRepository.save(a);
            } else if ("table".equals(qType)) {
                List<JSONObject> rows = filledRows(answer.optJSONArray("rows"));
                if (rows.isEmpty()) continue;
                QSAnswer a = new QSAnswer();
                a.setResponseId(response.getId());
                a.setQuestionId(q.getId());
                a.setTableRowsJson(new JSONArray(rows).toString());
                answerRepository.save(a);
            } else {
                List<Integer> optionIds = answer.has("optionIds") ? toIntList(answer.getJSONArray("optionIds")) : List.of();
                if (optionIds.isEmpty()) continue;
                QSAnswer a = new QSAnswer();
                a.setResponseId(response.getId());
                a.setQuestionId(q.getId());
                a = answerRepository.save(a);
                for (Integer optionId : optionIds) {
                    QSAnswerOption ao = new QSAnswerOption();
                    ao.setAnswerId(a.getId());
                    ao.setOptionId(optionId);
                    answerOptionRepository.save(ao);
                }
            }
        }

        return response.getId();
    }

    /** {questionId, prompt, questionType, textValue?, selectedLabels: [...]} per answered question — for the approver review panel. */
    public JSONArray getAnswersForReview(Integer responseId) {
        JSONArray out = new JSONArray();
        if (responseId == null) return out;
        List<QSAnswer> answers = answerRepository.findByResponseId(responseId);
        if (answers.isEmpty()) return out;

        List<Integer> questionIds = answers.stream().map(QSAnswer::getQuestionId).collect(Collectors.toList());
        Map<Integer, QSQuestion> questionsById = questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(QSQuestion::getId, q -> q));
        List<QSQuestionColumn> reviewColumns = questionIds.isEmpty() ? List.of() : columnRepository.findByQuestionIdInOrderByPosition(questionIds);
        Map<Integer, List<QSQuestionColumn>> reviewColumnsByQuestion = reviewColumns.stream()
                .collect(Collectors.groupingBy(QSQuestionColumn::getQuestionId, LinkedHashMap::new, Collectors.toList()));

        List<Integer> answerIds = answers.stream().map(QSAnswer::getId).collect(Collectors.toList());
        List<QSAnswerOption> selections = answerOptionRepository.findByAnswerIdIn(answerIds);
        Set<Integer> optionIds = selections.stream().map(QSAnswerOption::getOptionId).collect(Collectors.toSet());
        Map<Integer, String> labelsByOptionId = optionIds.isEmpty() ? Map.of() :
                optionRepository.findAllById(optionIds).stream().collect(Collectors.toMap(QSQuestionOption::getId, QSQuestionOption::getLabel));
        Map<Integer, List<Integer>> optionIdsByAnswer = selections.stream()
                .collect(Collectors.groupingBy(QSAnswerOption::getAnswerId, Collectors.mapping(QSAnswerOption::getOptionId, Collectors.toList())));

        for (QSAnswer a : answers) {
            QSQuestion q = questionsById.get(a.getQuestionId());
            if (q == null) continue;
            JSONObject out1 = new JSONObject();
            out1.put("questionId", q.getId());
            out1.put("prompt", q.getPrompt());
            out1.put("questionType", q.getQuestionType());
            out1.put("textValue", a.getTextValue());
            JSONArray labels = new JSONArray();
            for (Integer optId : optionIdsByAnswer.getOrDefault(a.getId(), List.of())) {
                labels.put(labelsByOptionId.getOrDefault(optId, "—"));
            }
            out1.put("selectedLabels", labels);

            List<QSQuestionColumn> cols = reviewColumnsByQuestion.getOrDefault(q.getId(), List.of());
            Map<String, String> columnLabelById = cols.stream()
                    .collect(Collectors.toMap(c -> String.valueOf(c.getId()), QSQuestionColumn::getLabel));
            JSONArray rowsOut = new JSONArray();
            if (a.getTableRowsJson() != null) {
                JSONArray storedRows = new JSONArray(a.getTableRowsJson());
                for (int i = 0; i < storedRows.length(); i++) {
                    JSONObject storedRow = storedRows.getJSONObject(i);
                    JSONObject rowOut = new JSONObject();
                    for (String key : storedRow.keySet()) {
                        rowOut.put(columnLabelById.getOrDefault(key, "(removed)"), storedRow.getString(key));
                    }
                    rowsOut.put(rowOut);
                }
            }
            out1.put("rows", rowsOut);
            JSONArray columnLabelsOut = new JSONArray();
            for (QSQuestionColumn c : cols) columnLabelsOut.put(c.getLabel());
            out1.put("columnLabels", columnLabelsOut);

            out.put(out1);
        }
        return out;
    }

    private static List<Integer> toIntList(JSONArray arr) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) out.add(arr.getInt(i));
        return out;
    }

    /** A table row counts as filled if at least one of its cells is non-blank — an all-blank
     *  row (e.g. a row the respondent added then left empty) is dropped rather than stored. */
    private static List<JSONObject> filledRows(JSONArray rowsArr) {
        List<JSONObject> out = new ArrayList<>();
        if (rowsArr == null) return out;
        for (int i = 0; i < rowsArr.length(); i++) {
            JSONObject row = rowsArr.getJSONObject(i);
            boolean anyFilled = false;
            for (String key : row.keySet()) {
                if (!row.optString(key, "").trim().isEmpty()) { anyFilled = true; break; }
            }
            if (anyFilled) out.add(row);
        }
        return out;
    }

    private static boolean hasFilledRow(JSONArray rowsArr) {
        return !filledRows(rowsArr).isEmpty();
    }

    private static boolean cellIsValid(String columnType, String value) {
        if ("number".equals(columnType)) {
            try { Double.parseDouble(value); return true; } catch (NumberFormatException e) { return false; }
        }
        if ("date".equals(columnType)) {
            try { java.time.LocalDate.parse(value); return true; } catch (java.time.format.DateTimeParseException e) { return false; }
        }
        return true;
    }
}
