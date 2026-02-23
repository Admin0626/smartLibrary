package com.example.smart_library.controller;

import com.example.smart_library.common.PageResponse;
import com.example.smart_library.common.Result;
import com.example.smart_library.dto.request.SearchBookRequest;
import com.example.smart_library.dto.response.BookInfoResponse;
import com.example.smart_library.entity.Book;
import com.example.smart_library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书控制器
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 创建图书（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BookInfoResponse> createBook(@Valid @RequestBody Book book) {
        BookInfoResponse response = bookService.createBook(book);
        return Result.success("创建成功", response);
    }

    /**
     * 获取图书详情
     */
    @GetMapping("/{bookId}")
    public Result<BookInfoResponse> getBookById(@PathVariable Long bookId) {
        BookInfoResponse response = bookService.getBookById(bookId);
        return Result.success(response);
    }

    /**
     * 根据ISBN获取图书
     */
    @GetMapping("/isbn/{isbn}")
    public Result<BookInfoResponse> getBookByIsbn(@PathVariable String isbn) {
        BookInfoResponse response = bookService.getBookByIsbn(isbn);
        return Result.success(response);
    }

    /**
     * 更新图书信息（管理员）
     */
    @PutMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BookInfoResponse> updateBook(@PathVariable Long bookId,
                                               @Valid @RequestBody Book book) {
        BookInfoResponse response = bookService.updateBook(bookId, book);
        return Result.success("更新成功", response);
    }

    /**
     * 删除图书（管理员）
     */
    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteBook(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return Result.success("删除成功");
    }

    /**
     * 搜索图书
     */
    @GetMapping("/search")
    public Result<PageResponse<BookInfoResponse>> searchBooks(@Valid SearchBookRequest request) {
        Page<BookInfoResponse> page = bookService.searchBooks(request);
        PageResponse<BookInfoResponse> response = PageResponse.of(page);
        return Result.success(response);
    }

    /**
     * 获取可借阅图书列表
     */
    @GetMapping("/available")
    public Result<PageResponse<BookInfoResponse>> getAvailableBooks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<BookInfoResponse> page = bookService.getAvailableBooks(pageNum, pageSize);
        PageResponse<BookInfoResponse> response = PageResponse.of(page);
        return Result.success(response);
    }

    /**
     * 获取热门图书
     */
    @GetMapping("/popular")
    public Result<List<BookInfoResponse>> getPopularBooks(@RequestParam(defaultValue = "10") Integer limit) {
        List<BookInfoResponse> response = bookService.getPopularBooks(limit);
        return Result.success(response);
    }
}
