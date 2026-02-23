package com.example.smart_library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 还书请求DTO
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnBookRequest {

    /**
     * 借阅记录ID
     */
    @NotNull(message = "借阅记录ID不能为空")
    private Long borrowRecordId;

    /**
     * 备注（如图书损坏情况等）
     */
    private String remark;
}
