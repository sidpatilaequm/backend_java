# Database Schema Reference Documentation
This document contains all entity classes, their mapped tables, columns, data types, and foreign key relationships as defined in the Spring Boot project. It is formatted for use by the Data Analysis team.
## Entity: `Attribute` (Table: `attribute`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `attributeId` | `Long` | `attribute_id` | Primary Key |
| `attributeName` | `String` | `attribute_name` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |
| `type` | `AttributeType` | `type` |  |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |

---
## Entity: `Authorization` (Table: `authorization`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `authId` | `int` | `auth_id` | Primary Key |
| `authName` | `String` | `auth_name` |  |
| `authKey` | `String` | `auth_key` |  |
| `active` | `boolean` | `is_active` |  |

---
## Entity: `BusinessCard` (Table: `business_cards`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `name` | `String` | `name` |  |
| `jobTitle` | `String` | `job_title` |  |
| `companyName` | `String` | `company_name` |  |
| `address` | `String` | `address` |  |
| `phoneNumbers` | `List<String>` | `phone_number` | Foreign Key Mapping |
| `emailAddresses` | `List<String>` | `email_address` | Foreign Key Mapping |
| `websiteUrl` | `String` | `website_url` |  |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |

---
## Entity: `Cart` (Table: `cart`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `cartId` | `Long` | `cart_id` | Primary Key |
| `material` | `Material` | `material_id` | Foreign Key Mapping |
| `materialName` | `String` | `material_name` |  |
| `materialCode` | `String` | `material_code` |  |
| `price` | `BigDecimal` | `price` |  |
| `totalPrice` | `BigDecimal` | `total_price` |  |
| `channel` | `Channel` | `channel_id` | Foreign Key Mapping |
| `channelCode` | `String` | `channel_code` |  |
| `channelName` | `String` | `channel_name` |  |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `companyName` | `String` | `company_name` |  |
| `imageId` | `Long` | `image_id` |  |
| `imageName` | `String` | `image_name` |  |
| `imageType` | `String` | `image_type` |  |
| `imageBase64` | `String` | `image_base64` |  |
| `addedAt` | `LocalDateTime` | `added_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `CategoryChannelMapping` (Table: `category_channel_mapping`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `internalCategory` | `ItemCategory` | `internal_category_id` | Foreign Key Mapping |
| `channelCategory` | `ChannelCategory` | `channel_category_id` | Foreign Key Mapping |
| `channel` | `Channel` | `channel_id` | Foreign Key Mapping |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `CertificateOfIncorporation` (Table: `certificate_of_incorporation`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `certificateOfIncorporationId` | `Long` | `certificateOfIncorporationId` | Primary Key |
| `cinNumber` | `String` | `cin_number` |  |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `businessName` | `String` | `businessName` |  |
| `rocCode` | `String` | `rocCode` |  |
| `registrationNumber` | `String` | `registrationNumber` |  |
| `category` | `String` | `category` |  |
| `subCategory` | `String` | `subCategory` |  |
| `companyClass` | `String` | `company_class` |  |
| `authorizedCapital` | `String` | `authorizedCapital` |  |
| `paidCapital` | `String` | `paidCapital` |  |
| `incorporatedDate` | `String` | `incorporatedDate` |  |
| `email` | `String` | `email` |  |
| `listed` | `Boolean` | `listed` |  |
| `lastAGMDate` | `String` | `lastAGMDate` |  |
| `lastBSDate` | `String` | `lastBSDate` |  |
| `active` | `Boolean` | `active` |  |
| `status` | `String` | `status` |  |
| `inc22AFiled` | `Boolean` | `inc22AFiled` |  |
| `soatDate` | `String` | `soatDate` |  |
| `regionalDirector` | `String` | `regionalDirector` |  |
| `region` | `String` | `region` |  |
| `suspendedAtStockExchange` | `Boolean` | `suspendedAtStockExchange` |  |
| `insolvencyStatus` | `String` | `insolvencyStatus` |  |
| `subscribedCapital` | `String` | `subscribedCapital` |  |
| `incorporatedCountry` | `String` | `incorporatedCountry` |  |
| `officeType` | `String` | `officeType` |  |
| `companyType` | `String` | `companyType` |  |
| `type` | `String` | `type` |  |
| `addressesJson` | `String` | `addressesJson` |  |
| `directorsJson` | `String` | `directorsJson` |  |
| `chargesJson` | `String` | `chargesJson` |  |
| `efilingsJson` | `String` | `efilingsJson` |  |
| `indexId` | `String` | `indexId` |  |
| `updatedTimestamp` | `Long` | `updatedTimestamp` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `Channel` (Table: `channel`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `channelId` | `Long` | `channel_id` | Primary Key |
| `channelName` | `String` | `channel_name` |  |
| `channelCode` | `String` | `channel_code` |  |
| `description` | `String` | `description` |  |
| `status` | `String` | `status` |  |
| `country` | `Country` | `country_id` | Foreign Key Mapping |
| `currency` | `Currency` | `currency_id` | Foreign Key Mapping |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `user` | `UserDetail` | `user_id` | Foreign Key Mapping |
| `categories` | `List<ChannelCategory>` | `categories` | One-to-Many Relationship (mapped by channel) |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `ChannelCategory` (Table: `channel_category`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `categoryId` | `Long` | `category_id` | Primary Key |
| `categoryCode` | `String` | `category_code` |  |
| `categoryName` | `String` | `category_name` |  |
| `channel` | `Channel` | `channel_id` | Foreign Key Mapping |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `parentCategory` | `ChannelCategory` | `parent_category_id` | Foreign Key Mapping |
| `levelNo` | `Integer` | `level_no` |  |
| `fullPath` | `String` | `full_path` |  |
| `externalCategoryId` | `String` | `external_category_id` |  |
| `externalParentId` | `String` | `external_parent_id` |  |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `deletedAt` | `LocalDateTime` | `deleted_at` |  |

---
## Entity: `ChannelCategoryAttribute` (Table: `channel_category_attribute`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `channelCategory` | `ChannelCategory` | `channel_category_id` | Foreign Key Mapping |
| `attributeName` | `String` | `attribute_name` |  |
| `attributeCode` | `String` | `attribute_code` |  |
| `allowedValues` | `String` | `allowed_values` |  |
| `defaultValue` | `String` | `default_value` |  |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `ChequeDetails` (Table: `chequedetails`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `chequeDetailsId` | `Long` | `chequeDetailsId` | Primary Key |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `bank` | `String` | `bank` |  |
| `code` | `String` | `code` |  |
| `issuedTo` | `String` | `issuedTo` |  |
| `signatory` | `String` | `signatory` |  |
| `accountNumber` | `String` | `accountNumber` |  |
| `ifsc` | `String` | `ifsc` |  |
| `issued` | `String` | `issued` |  |
| `branch` | `String` | `branch` |  |

---
## Entity: `CompanyCoverPhoto` (Table: `company_cover_photos`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `coverPhotoId` | `Long` | `cover_photo_id` | Primary Key |
| `companyId` | `Long` | `company_id` |  |
| `coverPhotoName` | `String` | `cover_photo_name` |  |
| `coverPhotoType` | `String` | `cover_photo_type` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |
| `createdBy` | `String` | `created_by` |  |
| `modifiedBy` | `String` | `modified_by` |  |

---
## Entity: `CompanyDetails` (Table: `company_details`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `companyId` | `Long` | `companyId` | Primary Key |
| `companyName` | `String` | `company_name` |  |
| `companyCode` | `String` | `company_code` |  |
| `status` | `String` | `status` |  |
| `country` | `Country` | `country_id` | Foreign Key Mapping |
| `currency` | `Currency` | `currency_id` | Foreign Key Mapping |
| `gstinNumber` | `String` | `gstin_number` |  |
| `legalTradeName` | `String` | `legal_trade_name` |  |
| `registeredAddress` | `String` | `registered_address` |  |
| `panNumber` | `String` | `pan_number` |  |
| `panTinCst` | `String` | `pan_tin_cst` |  |
| `dateOfRegistration` | `LocalDate` | `date_of_registration` |  |
| `typeOfRegistration` | `String` | `type_of_registration` |  |
| `authKey` | `String` | `auth_key` |  |
| `gstFileName` | `String` | `gst_file_name` |  |
| `panFileName` | `String` | `pan_file_name` |  |
| `chequeFileName` | `String` | `cheque_file_name` |  |
| `coiFileName` | `String` | `coi_file_name` |  |
| `user` | `UserDetail` | `user_id` | Foreign Key Mapping |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |
| `panDetails` | `PanDetails` | `panDetails` | One-to-One Relationship (mapped by company) |
| `chequeDetails` | `ChequeDetails` | `chequeDetails` | One-to-One Relationship (mapped by company) |
| `certificateOfIncorporation` | `CertificateOfIncorporation` | `certificateOfIncorporation` | One-to-One Relationship (mapped by company) |
| `msmeDetails` | `MsmeDetails` | `msmeDetails` | One-to-One Relationship (mapped by company) |
| `itrDetails` | `ItrDetails` | `itrDetails` | One-to-One Relationship (mapped by company) |
| `purchasingData` | `PurchasingData` | `purchasingData` | One-to-One Relationship (mapped by company) |
| `financialTerms` | `List<FinancialTerms>` | `financialTerms` | One-to-Many Relationship (mapped by company) |

---
## Entity: `Country` (Table: `country`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `countryId` | `Long` | `country_id` | Primary Key |
| `countryName` | `String` | `country_name` |  |
| `isoCode` | `String` | `iso_code` |  |
| `phoneCode` | `String` | `phone_code` |  |
| `status` | `String` | `status` |  |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |

---
## Entity: `Currency` (Table: `currency`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `currencyId` | `Long` | `currency_id` | Primary Key |
| `currencyCode` | `String` | `currency_code` |  |
| `currencyName` | `String` | `currency_name` |  |
| `symbol` | `String` | `symbol` |  |
| `status` | `String` | `status` |  |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |

---
## Entity: `FileUpload` (Table: `file_uploads`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `fileId` | `Long` | `fileId` | Primary Key |
| `fileName` | `String` | `fileName` |  |
| `fileType` | `String` | `fileType` |  |
| `documentType` | `String` | `documentType` |  |
| `filePath` | `String` | `filePath` |  |
| `uploadDate` | `LocalDateTime` | `uploadDate` |  |
| `user` | `UserDetail` | `user_id` | Foreign Key Mapping |

---
## Entity: `FinancialTerms` (Table: `financial_terms`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `financialTermsId` | `Long` | `financialTermsId` | Primary Key |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `deliveryTerms` | `String` | `delivery_terms` |  |
| `deliveryLocation` | `String` | `delivery_location` |  |
| `blockIndicator` | `String` | `block_indicator` |  |
| `orderCurrency` | `String` | `order_currency` |  |
| `deliveryDays` | `String` | `delivery_days` |  |
| `reconciliationAccount` | `String` | `reconciliation_account` |  |
| `termsOfPayment` | `String` | `terms_of_payment` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `FinancialTermsCustomer` (Table: `financial_terms_customer`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `financialTermsCustomerId` | `Long` | `financialTermsCustomerId` | Primary Key |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `deliveryTerms` | `String` | `delivery_terms` |  |
| `deliveryLocation` | `String` | `delivery_location` |  |
| `blockIndicator` | `String` | `block_indicator` |  |
| `orderCurrency` | `String` | `order_currency` |  |
| `deliveryDays` | `String` | `delivery_days` |  |
| `reconciliationAccount` | `String` | `reconciliation_account` |  |
| `termsOfPayment` | `String` | `terms_of_payment` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `FlipbookDocument` (Table: `flipbook_documents`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `docKey` | `String` | `doc_key` |  |
| `storageUrl` | `String` | `storage_url` |  |
| `fileSize` | `Long` | `file_size` |  |
| `pages` | `Integer` | `pages` |  |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `FlipbookHotspots` (Table: `flipbook_hotspots`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `docKey` | `String` | `doc_key` |  |
| `totalPages` | `Integer` | `total_pages` |  |
| `hotspotsJson` | `String` | `hotspots_json` |  |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `Inventory` (Table: `inventory`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `inventoryId` | `Long` | `inventory_id` | Primary Key |
| `material` | `Material` | `material_id` | Foreign Key Mapping |
| `variant` | `MaterialVariant` | `variant_id` | Foreign Key Mapping |
| `location` | `Location` | `location_id` | Foreign Key Mapping |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |
| `price` | `BigDecimal` | `price` |  |
| `availableQty` | `Double` | `available_qty` |  |
| `reservedQty` | `Double` | `reserved_qty` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `ItemCategory` (Table: `item_category`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `itemCategoryId` | `Long` | `item_category_id` | Primary Key |
| `code` | `String` | `code` |  |
| `description` | `String` | `description` |  |
| `categoryName` | `String` | `category_name` |  |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |
| `parentId` | `Long` | `parent_id` |  |

---
## Entity: `ItemSubcategory` (Table: `item_subcategory`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `itemSubcategoryId` | `Long` | `item_subcategory_id` | Primary Key |
| `itemSubcategoryName` | `String` | `item_subcategory_name` |  |
| `itemCategory` | `ItemCategory` | `item_category_id` | Foreign Key Mapping |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `parentSubcategory` | `ItemSubcategory` | `parent_subcategory_id` | Foreign Key Mapping |
| `levelNo` | `Integer` | `level_no` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `ItrDetails` (Table: `vendor_itr`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `itrDetailsId` | `Long` | `itrDetailsId` | Primary Key |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `pan` | `String` | `pan` |  |
| `birthOrIncorporatedDate` | `String` | `birthOrIncorporatedDate` |  |
| `name` | `String` | `name` |  |
| `fy` | `String` | `fy` |  |
| `itrFiled` | `Boolean` | `itrFiled` |  |
| `itrType` | `String` | `itrType` |  |
| `grossTurnover` | `String` | `grossTurnover` |  |
| `grossTurnoverFormatted` | `String` | `grossTurnoverFormatted` |  |
| `exportTurnover` | `String` | `exportTurnover` |  |
| `exportTurnoverFormatted` | `String` | `exportTurnoverFormatted` |  |
| `valid` | `Boolean` | `valid` |  |
| `panStatus` | `String` | `panStatus` |  |
| `message` | `String` | `message` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `ListingAttributeValue` (Table: `listing_attribute_value`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `listing` | `MaterialChannelListing` | `listing_id` | Foreign Key Mapping |
| `attribute` | `ChannelCategoryAttribute` | `attribute_id` | Foreign Key Mapping |
| `attributeValue` | `String` | `attribute_value` |  |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `Location` (Table: `location`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `locationId` | `Long` | `location_id` | Primary Key |
| `locationName` | `String` | `location_name` |  |
| `pinCode` | `String` | `pin_code` |  |
| `address` | `String` | `address` |  |
| `city` | `String` | `city` |  |
| `state` | `String` | `state` |  |
| `country` | `String` | `country` |  |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |
| `materials` | `List<Material>` | `materials` | One-to-Many Relationship (mapped by location) |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `MasterBomFile` (Table: `master_bom_files`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `fileName` | `String` | `file_name` |  |
| `uploadedAt` | `LocalDateTime` | `uploaded_at` |  |
| `uploadedBy` | `String` | `uploaded_by` |  |
| `filePath` | `String` | `file_path` |  |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |

---
## Entity: `MasterBomRecord` (Table: `master_bom_records`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `masterFile` | `MasterBomFile` | `master_file_id` | Foreign Key Mapping |
| `fgNumber` | `String` | `fg_number` |  |
| `description` | `String` | `description` |  |
| `rmItemCode` | `String` | `rm_item_code` |  |
| `rmDescription` | `String` | `rm_description` |  |
| `qty` | `Double` | `qty` |  |
| `uom` | `String` | `uom` |  |

---
## Entity: `Material` (Table: `material`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `materialId` | `Long` | `material_id` | Primary Key |
| `materialName` | `String` | `material_name` |  |
| `description` | `String` | `description` |  |
| `type` | `String` | `type` |  |
| `baseUnitOfMeasure` | `String` | `base_unit_of_measure` |  |
| `subcategory` | `ItemSubcategory` | `item_subcategory_id` | Foreign Key Mapping |
| `subcategoryL1` | `ItemSubcategory` | `subcategory_l1_id` | Foreign Key Mapping |
| `subcategoryL2` | `ItemSubcategory` | `subcategory_l2_id` | Foreign Key Mapping |
| `subcategoryL3` | `ItemSubcategory` | `subcategory_l3_id` | Foreign Key Mapping |
| `itemCategory` | `ItemCategory` | `item_category_id` | Foreign Key Mapping |
| `hsnCode` | `String` | `hsn_code` |  |
| `sku` | `String` | `sku` |  |
| `purchasingCode` | `String` | `purchasing_code` |  |
| `vendorArticleNumber` | `String` | `vendor_article_number` |  |
| `materialCode` | `String` | `material_code` |  |
| `status` | `String` | `status` |  |
| `materialImages` | `List<MaterialImage>` | `materialImages` | One-to-Many Relationship (mapped by material) |
| `variants` | `Set<MaterialVariant>` | `variants` | One-to-Many Relationship (mapped by material) |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |
| `location` | `Location` | `location_id` | Foreign Key Mapping |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |
| `generalAttributes` | `List<MaterialAttribute>` | `generalAttributes` | One-to-Many Relationship (mapped by material) |

---
## Entity: `MaterialAttribute` (Table: `material_attribute`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `material` | `Material` | `material_id` | Foreign Key Mapping |
| `attribute` | `Attribute` | `attribute_id` | Foreign Key Mapping |
| `attributeValue` | `String` | `attribute_value` |  |
| `variant` | `MaterialVariant` | `variant_id` | Foreign Key Mapping |

---
## Entity: `MaterialBomExcel` (Table: `material_bom_excel`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `material` | `Material` | `material_id` | Foreign Key Mapping |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |
| `filePath` | `String` | `file_path` |  |
| `fileName` | `String` | `file_name` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `MaterialChannelListing` (Table: `material_channel_listing`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `material` | `Material` | `material_id` | Foreign Key Mapping |
| `channel` | `Channel` | `channel_id` | Foreign Key Mapping |
| `channelCategory` | `ChannelCategory` | `channel_category_id` | Foreign Key Mapping |
| `channelSku` | `String` | `channel_sku` |  |
| `sellingPrice` | `BigDecimal` | `selling_price` |  |
| `mrp` | `BigDecimal` | `mrp` |  |
| `availableStock` | `Integer` | `available_stock` |  |
| `validationStatus` | `String` | `validation_status` |  |
| `lastSyncedAt` | `LocalDateTime` | `last_synced_at` |  |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |
| `createdBy` | `String` | `created_by` |  |
| `updatedBy` | `String` | `updated_by` |  |
| `approvedBy` | `String` | `approved_by` |  |
| `approvedAt` | `LocalDateTime` | `approved_at` |  |
| `deletedAt` | `LocalDateTime` | `deleted_at` |  |

---
## Entity: `MaterialChannelMapping` (Table: `material_channel_mapping`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `material` | `Material` | `material_id` | Foreign Key Mapping |
| `channel` | `Channel` | `channel_id` | Foreign Key Mapping |
| `category` | `ChannelCategory` | `category_id` | Foreign Key Mapping |
| `price` | `BigDecimal` | `price` |  |
| `stock` | `Integer` | `stock` |  |
| `channelSku` | `String` | `channel_sku` |  |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `MaterialImage` (Table: `material_images`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `imageId` | `Long` | `image_id` | Primary Key |
| `material` | `Material` | `material_id` | Foreign Key Mapping |
| `imageName` | `String` | `image_name` |  |
| `imageType` | `String` | `image_type` |  |
| `sequenceOrder` | `Integer` | `sequence_order` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `MaterialVariant` (Table: `material_variant`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `material` | `Material` | `material_id` | Foreign Key Mapping |
| `variantCode` | `String` | `variant_code` |  |
| `mrp` | `Double` | `mrp` |  |
| `sellingPrice` | `Double` | `selling_price` |  |
| `cost` | `Double` | `cost` |  |
| `stock` | `Double` | `stock` |  |
| `attributes` | `List<MaterialAttribute>` | `variant_image` | One-to-Many Relationship (mapped by variant) |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `MsmeDetails` (Table: `vendor_msme`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `msmeDetailsId` | `Long` | `msmeDetailsId` | Primary Key |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `udyamNumber` | `String` | `udyamNumber` |  |
| `entityName` | `String` | `entityName` |  |
| `type` | `String` | `type` |  |
| `majorActivity` | `String` | `majorActivity` |  |
| `gender` | `String` | `gender` |  |
| `socialCategory` | `String` | `socialCategory` |  |
| `incorporatedDate` | `String` | `incorporatedDate` |  |
| `commencedDate` | `String` | `commencedDate` |  |
| `registeredDate` | `String` | `registeredDate` |  |
| `classifications` | `String` | `classifications` |  |
| `locations` | `String` | `locations` |  |
| `officialAddress` | `String` | `officialAddress` |  |
| `nicCodes` | `String` | `nicCodes` |  |
| `dic` | `String` | `dic` |  |
| `dfo` | `String` | `dfo` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `Order` (Table: `orders`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `orderId` | `Long` | `order_id` | Primary Key |
| `orderNumber` | `String` | `order_number` |  |
| `customerFirstName` | `String` | `customer_first_name` |  |
| `customerLastName` | `String` | `customer_last_name` |  |
| `customerEmail` | `String` | `customer_email` |  |
| `customerPhone` | `String` | `customer_phone` |  |
| `customerAddress` | `String` | `customer_address` |  |
| `customerCity` | `String` | `customer_city` |  |
| `customerState` | `String` | `customer_state` |  |
| `customerZipCode` | `String` | `customer_zip_code` |  |
| `customerCountry` | `String` | `customer_country` |  |
| `customerNotes` | `String` | `customer_notes` |  |
| `totalItems` | `Integer` | `total_items` |  |
| `subtotal` | `BigDecimal` | `subtotal` |  |
| `total` | `BigDecimal` | `total` |  |
| `orderDate` | `LocalDateTime` | `order_date` |  |
| `channelId` | `Long` | `channel_id` |  |
| `companyId` | `Long` | `company_id` |  |
| `orderItems` | `List<OrderItem>` | `order_status` | One-to-Many Relationship (mapped by order) |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `OrderItem` (Table: `order_items`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `orderItemId` | `Long` | `order_item_id` | Primary Key |
| `order` | `Order` | `order_id` | Foreign Key Mapping |
| `materialId` | `Long` | `material_id` |  |
| `materialName` | `String` | `material_name` |  |
| `materialCode` | `String` | `material_code` |  |
| `price` | `BigDecimal` | `price` |  |
| `quantity` | `Integer` | `quantity` |  |
| `totalPrice` | `BigDecimal` | `total_price` |  |
| `channelId` | `Long` | `channel_id` |  |
| `imageId` | `Long` | `image_id` |  |
| `imageName` | `String` | `image_name` |  |
| `imageType` | `String` | `image_type` |  |
| `imageBase64` | `String` | `image_base64` |  |
| `addedAt` | `LocalDateTime` | `added_at` |  |

---
## Entity: `PanDetails` (Table: `pandetails`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `panDetailsId` | `Long` | `panDetailsId` | Primary Key |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `panNumber` | `String` | `panNumber` |  |
| `name` | `String` | `name` |  |
| `dateOfBirthIncorporation` | `String` | `dateOfBirthIncorporation` |  |
| `fathersName` | `String` | `fathersName` |  |
| `category` | `String` | `category` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `PermissionMaster` (Table: `permission_master`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `code` | `String` | `code` |  |
| `name` | `String` | `name` |  |
| `type` | `PermissionType` | `type` |  |
| `parent` | `PermissionMaster` | `parent_id` | Foreign Key Mapping |

---
## Entity: `PurchaseRequisition` (Table: `purchase_requisitions`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `prNumber` | `String` | `prNumber` |  |
| `locationId` | `Long` | `locationId` |  |
| `requestedBy` | `Long` | `requestedBy` |  |
| `requiredDate` | `LocalDate` | `requiredDate` |  |
| `remarks` | `String` | `remarks` |  |
| `totalAmount` | `BigDecimal` | `totalAmount` |  |
| `createdAt` | `Timestamp` | `createdAt` |  |
| `updatedAt` | `Timestamp` | `updatedAt` |  |

---
## Entity: `PurchaseRequisitionItem` (Table: `purchase_requisition_items`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `purchaseRequisition` | `PurchaseRequisition` | `purchase_requisition_id` | Foreign Key Mapping |
| `materialId` | `Long` | `materialId` |  |
| `sku` | `String` | `sku` |  |
| `quantity` | `BigDecimal` | `quantity` |  |
| `uom` | `String` | `uom` |  |
| `estimatedPrice` | `BigDecimal` | `estimatedPrice` |  |
| `totalPrice` | `BigDecimal` | `totalPrice` |  |

---
## Entity: `PurchaseRequisitionItemVendor` (Table: `purchase_requisition_item_vendors`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `purchaseRequisitionItem` | `PurchaseRequisitionItem` | `purchase_requisition_item_id` | Foreign Key Mapping |
| `vendorId` | `Long` | `vendorId` |  |
| `sentAt` | `Timestamp` | `sentAt` |  |

---
## Entity: `PurchasingData` (Table: `purchasing_data`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `purchasingOrg` | `String` | `purchasing_org` |  |
| `orderCurrency` | `String` | `order_currency` |  |
| `incoterms` | `String` | `incoterms` |  |
| `termsOfPayment` | `String` | `terms_of_payment` |  |
| `vendorSchemaGroup` | `String` | `vendor_schema_group` |  |
| `minimumOrderValue` | `BigDecimal` | `minimum_order_value` |  |
| `deliveryDays` | `Integer` | `delivery_days` |  |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |

---
## Entity: `SuperAdmin` (Table: `super_admin`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `superAdminId` | `Long` | `superAdminId` | Primary Key |
| `email` | `String` | `email` |  |
| `password` | `String` | `password` |  |
| `firstName` | `String` | `firstName` |  |
| `lastName` | `String` | `lastName` |  |
| `phoneNumber` | `String` | `phoneNumber` |  |
| `signupDate` | `String` | `signupDate` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `UserAuthentication` (Table: `user_authentication`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `userAuthenticationId` | `Long` | `user_authentication_id` | Primary Key |
| `userId` | `Long` | `user_id` |  |
| `authKey` | `String` | `auth_key` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `UserDetail` (Table: `user_details`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `userId` | `Long` | `userId` | Primary Key |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |
| `email` | `String` | `email` |  |
| `password` | `String` | `password` |  |
| `firstName` | `String` | `firstName` |  |
| `lastName` | `String` | `lastName` |  |
| `phoneNumber` | `String` | `phoneNumber` |  |
| `signupDate` | `String` | `signupDate` |  |
| `designation` | `String` | `designation` |  |
| `onboardingStatus` | `String` | `onboardingStatus` |  |
| `onboardingToken` | `String` | `onboardingToken` |  |
| `tokenExpiry` | `LocalDateTime` | `tokenExpiry` |  |
| `userType` | `UserType` | `user_type` |  |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `VendorCatalogue` (Table: `vendor_catalogue`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `catalogueId` | `Long` | `catalogue_id` | Primary Key |
| `vendorId` | `Long` | `vendor_id` |  |
| `fileName` | `String` | `file_name` |  |
| `fileType` | `String` | `file_type` |  |
| `fileSize` | `Long` | `file_size` |  |
| `uploadDate` | `LocalDateTime` | `upload_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |
| `user` | `UserDetail` | `user_id` | Foreign Key Mapping |
| `superAdmin` | `SuperAdmin` | `super_admin_id` | Foreign Key Mapping |

---
## Entity: `VendorInvoice` (Table: `vendor_invoice`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `invoiceId` | `Long` | `invoice_id` | Primary Key |
| `invoiceNumber` | `String` | `invoice_number` |  |
| `poId` | `Long` | `po_id` |  |
| `vendorCompany` | `CompanyDetails` | `vendor_company_id` | Foreign Key Mapping |
| `vendorUser` | `UserDetail` | `vendor_user_id` | Foreign Key Mapping |
| `invoiceDate` | `LocalDate` | `invoice_date` |  |
| `invoiceDueDate` | `LocalDate` | `invoice_due_date` |  |
| `vendorNumber` | `String` | `vendor_number` |  |
| `gstNumber` | `String` | `gst_number` |  |
| `invoiceCurrency` | `String` | `invoice_currency` |  |
| `billType` | `String` | `bill_type` |  |
| `consignee` | `String` | `consignee` |  |
| `businessPlace` | `String` | `business_place` |  |
| `zipCode` | `String` | `zip_code` |  |
| `deliveryNoteNumber` | `String` | `delivery_note_number` |  |
| `sectionCode` | `String` | `section_code` |  |
| `tdsSection` | `String` | `tds_section` |  |
| `tdsRate` | `BigDecimal` | `tds_rate` |  |
| `sapTaxType` | `String` | `sap_tax_type` |  |
| `sapTaxCode` | `String` | `sap_tax_code` |  |
| `dcDate` | `LocalDate` | `dc_date` |  |
| `remarks` | `String` | `remarks` |  |
| `createdAt` | `LocalDateTime` | `created_at` |  |
| `updatedAt` | `LocalDateTime` | `updated_at` |  |

---
## Entity: `VendorInvoiceItem` (Table: `vendor_invoice_item`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `invoiceItemId` | `Long` | `invoice_item_id` | Primary Key |
| `vendorInvoice` | `VendorInvoice` | `invoice_id` | Foreign Key Mapping |
| `srNo` | `Integer` | `sr_no` |  |
| `packCode` | `String` | `pack_code` |  |
| `material` | `String` | `material` |  |
| `description` | `String` | `description` |  |
| `hsnCode` | `String` | `hsn_code` |  |
| `itemCode` | `String` | `item_code` |  |
| `batchNumber` | `String` | `batch_number` |  |
| `mrp` | `BigDecimal` | `mrp` |  |
| `quantity` | `BigDecimal` | `quantity` |  |
| `rate` | `BigDecimal` | `rate` |  |
| `currency` | `String` | `currency` |  |
| `discountAmount` | `BigDecimal` | `discount_amount` |  |
| `basicAmount` | `BigDecimal` | `basic_amount` |  |
| `cgstPercent` | `BigDecimal` | `cgst_percent` |  |
| `cgstAmount` | `BigDecimal` | `cgst_amount` |  |
| `sgstPercent` | `BigDecimal` | `sgst_percent` |  |
| `sgstAmount` | `BigDecimal` | `sgst_amount` |  |
| `ugstPercent` | `BigDecimal` | `ugst_percent` |  |
| `ugstAmount` | `BigDecimal` | `ugst_amount` |  |
| `igstPercent` | `BigDecimal` | `igst_percent` |  |
| `igstAmount` | `BigDecimal` | `igst_amount` |  |
| `lineTotal` | `BigDecimal` | `line_total` |  |

---
## Entity: `VendorPermission` (Table: `vendor_permission`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | Primary Key |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `permission` | `PermissionMaster` | `permission_id` | Foreign Key Mapping |

---
## Entity: `VendorQuotation` (Table: `vendor_quotations`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `quotationId` | `Long` | `quotation_id` | Primary Key |
| `purchaseRequisition` | `PurchaseRequisition` | `pr_id` | Foreign Key Mapping |
| `vendor` | `CompanyDetails` | `vendor_id` | Foreign Key Mapping |
| `quotationNumber` | `String` | `quotation_number` |  |
| `quotationDate` | `LocalDate` | `quotation_date` |  |
| `vendorReferenceNo` | `String` | `vendor_reference_no` |  |
| `currency` | `String` | `currency` |  |
| `validityDays` | `Integer` | `validity_days` |  |
| `validUntil` | `LocalDate` | `valid_until` |  |
| `paymentTermsId` | `Long` | `payment_terms_id` |  |
| `advanceRequiredPercent` | `BigDecimal` | `advance_required_percent` |  |
| `bankGuaranteeRequired` | `Boolean` | `bank_guarantee_required` |  |
| `incoterm` | `String` | `incoterm` |  |
| `namedPlace` | `String` | `named_place` |  |
| `quotedDeliveryDate` | `LocalDate` | `quoted_delivery_date` |  |
| `leadTimeDays` | `Integer` | `lead_time_days` |  |
| `shippingMode` | `String` | `shipping_mode` |  |
| `freightChargeType` | `String` | `freight_charge_type` |  |
| `freightAmount` | `BigDecimal` | `freight_amount` |  |
| `coverNote` | `String` | `cover_note` |  |
| `internalNotes` | `String` | `internal_notes` |  |
| `quotationPdf` | `String` | `quotation_pdf` |  |
| `status` | `String` | `status` |  |
| `subtotalAmount` | `BigDecimal` | `subtotal_amount` |  |
| `gstTotalAmount` | `BigDecimal` | `gst_total_amount` |  |
| `grandTotalAmount` | `BigDecimal` | `grand_total_amount` |  |
| `createdDate` | `LocalDateTime` | `created_date` |  |
| `modifiedDate` | `LocalDateTime` | `modified_date` |  |

---
## Entity: `VendorQuotationDocument` (Table: `vendor_quotation_documents`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `documentId` | `Long` | `document_id` | Primary Key |
| `vendorQuotation` | `VendorQuotation` | `quotation_id` | Foreign Key Mapping |
| `documentType` | `String` | `document_type` |  |
| `filePath` | `String` | `file_path` |  |
| `fileName` | `String` | `file_name` |  |
| `fileSize` | `Long` | `file_size` |  |
| `fileType` | `String` | `file_type` |  |
| `uploadedAt` | `LocalDateTime` | `uploaded_at` |  |

---
## Entity: `VendorQuotationItem` (Table: `vendor_quotation_items`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `quotationItemId` | `Long` | `quotation_item_id` | Primary Key |
| `vendorQuotation` | `VendorQuotation` | `quotation_id` | Foreign Key Mapping |
| `purchaseRequisitionItem` | `PurchaseRequisitionItem` | `pr_line_id` | Foreign Key Mapping |
| `itemCode` | `String` | `item_code` |  |
| `description` | `String` | `description` |  |
| `prQty` | `BigDecimal` | `pr_qty` |  |
| `quotedQty` | `BigDecimal` | `quoted_qty` |  |
| `uom` | `String` | `uom` |  |
| `unitPrice` | `BigDecimal` | `unit_price` |  |
| `gstPercent` | `BigDecimal` | `gst_percent` |  |
| `deliveryDate` | `LocalDate` | `delivery_date` |  |
| `paymentTermsId` | `Long` | `payment_terms_id` |  |
| `incoterm` | `String` | `incoterm` |  |
| `freightAmount` | `BigDecimal` | `freight_amount` |  |
| `lineTotal` | `BigDecimal` | `line_total` |  |
| `gstAmount` | `BigDecimal` | `gst_amount` |  |

---
## Entity: `VendorTerms` (Table: `vendorterms`)
| Field Name | Java Type | Database Column / Mapping | Details |
| :--- | :--- | :--- | :--- |
| `vendorTermsId` | `Long` | `vendorTermsId` | Primary Key |
| `user` | `UserDetail` | `user_id` | Foreign Key Mapping |
| `company` | `CompanyDetails` | `company_id` | Foreign Key Mapping |
| `paymentTermsFileName` | `String` | `delivery_terms_file` |  |
| `paymentTermsFileType` | `String` | `paymentTermsFileType` |  |
| `incotermsFileName` | `String` | `incotermsFileName` |  |
| `incotermsFileType` | `String` | `incotermsFileType` |  |
| `deliveryTermsFileName` | `String` | `deliveryTermsFileName` |  |
| `deliveryTermsFileType` | `String` | `deliveryTermsFileType` |  |
| `createdDate` | `LocalDateTime` | `createdDate` |  |
| `modifiedDate` | `LocalDateTime` | `modifiedDate` |  |

---
