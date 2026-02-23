package com.example.smart_library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预约图书请求DTO
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveBookRequest {

    /**
     * 图书ID
     */
    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    /**
     * 备注
     */
    private String remark;
}
