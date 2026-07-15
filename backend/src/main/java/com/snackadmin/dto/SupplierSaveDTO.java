package com.snackadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierSaveDTO {
    @NotBlank @Size(min = 2, max = 100) private String name;
    @NotBlank @Size(max = 30) private String contactName;
    @NotBlank @Size(max = 20) private String contactPhone;
    @Size(max = 200) private String address;
    @Size(max = 500) private String supplyMaterials;
    @NotBlank private String status;
    @Size(max = 200) private String remark;
}
