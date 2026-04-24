package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.CurrencyDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Currency;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrentUserService currentUserService;

    public ServiceResponse getAllCurrencies() {
        ServiceResponse response = new ServiceResponse();
        try {
            SuperAdmin currentAdmin = currentUserService.getCurrentSuperAdmin();
            List<Currency> currencies = currencyRepository.findBySuperAdmin(currentAdmin);
            List<CurrencyDTO> dtos = currencies.stream().map(this::convertToDTO).collect(Collectors.toList());
            response.setStatus("SUCCESS");
            response.setStatusMsg("Currencies retrieved successfully");
            response.addData("currencies", dtos);
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve currencies: " + e.getMessage());
        }
        return response;
    }

    public ServiceResponse getCurrencyById(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            SuperAdmin currentAdmin = currentUserService.getCurrentSuperAdmin();
            Optional<Currency> currency = currencyRepository.findById(id);
            if (currency.isPresent()
                    && currency.get().getSuperAdmin().getSuperAdminId().equals(currentAdmin.getSuperAdminId())) {
                response.setStatus("SUCCESS");
                response.setStatusMsg("Currency retrieved successfully");
                response.addData("currency", convertToDTO(currency.get()));
            } else {
                response.setStatus("ERROR");
                response.setStatusMsg("Currency not found");
            }
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve currency: " + e.getMessage());
        }
        return response;
    }

    public ServiceResponse saveCurrency(Currency currency) {
        ServiceResponse response = new ServiceResponse();
        try {
            SuperAdmin currentAdmin = currentUserService.getCurrentSuperAdmin();

            // Check for duplicates for the same admin
            if (currencyRepository.existsByCurrencyCodeAndSuperAdmin(currency.getCurrencyCode(), currentAdmin)) {
                Optional<Currency> existing = currencyRepository
                        .findByCurrencyCodeAndSuperAdmin(currency.getCurrencyCode(), currentAdmin);
                if (existing.isPresent() && !existing.get().getCurrencyId().equals(currency.getCurrencyId())) {
                    response.setStatus("ERROR");
                    response.setStatusMsg("Currency with this code already exists for your account");
                    return response;
                }
            }

            currency.setSuperAdmin(currentAdmin);
            Currency savedCurrency = currencyRepository.save(currency);
            response.setStatus("SUCCESS");
            response.setStatusMsg("Currency saved successfully");
            response.addData("currency", convertToDTO(savedCurrency));
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to save currency: " + e.getMessage());
        }
        return response;
    }

    public ServiceResponse deleteCurrency(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            currencyRepository.deleteById(id);
            response.setStatus("SUCCESS");
            response.setStatusMsg("Currency deleted successfully");
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to delete currency: " + e.getMessage());
        }
        return response;
    }

    private CurrencyDTO convertToDTO(Currency currency) {
        return new CurrencyDTO(
                currency.getCurrencyId(),
                currency.getCurrencyCode(),
                currency.getCurrencyName(),
                currency.getSymbol(),
                currency.getStatus());
    }
}
