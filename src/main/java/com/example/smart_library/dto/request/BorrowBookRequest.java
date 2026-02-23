package com.example.smart_library.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 借书请求DTO
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowBookRequest {

    /**
     * 图书ID
     */
    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    /**
     * 借阅天数（默认30天）
     */
    @Min(value = 1, message = "借阅天数至少为1天")
    private Integer borrowDays = 30;
}
