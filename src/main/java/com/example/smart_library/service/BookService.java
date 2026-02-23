package com.example.smart_library.service;

import com.example.smart_library.dto.request.SearchBookRequest;
import com.example.smart_library.dto.response.BookInfoResponse;
import com.example.smart_library.entity.Book;
import com.example.smart_library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 图书服务接口实现
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    /**
     * 创建图书
     *
     * @param book 图书信息
     * @return 图书信息
     */
    @Transactional
    public BookInfoResponse createBook(Book book) {
        // 检查ISBN是否已存在
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new RuntimeException("该ISBN的图书已存在");
        }

        // 设置初始状态
        book.setStatus(Book.BookStatus.AVAILABLE);
        book.setAvailableStock(book.getTotalStock());

        book = bookRepository.save(book);
        return convertToResponse(book);
    }

    /**
     * 根据ID获取图书信息
     *
     * @param bookId 图书ID
     * @return 图书信息
     */
    public BookInfoResponse getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        return convertToResponse(book);
    }

    /**
     * 根据ISBN获取图书信息
     *
     * @param isbn ISBN编号
     * @return 图书信息
     */
    public BookInfoResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        return convertToResponse(book);
    }

    /**
     * 更新图书信息
     *
     * @param bookId 图书ID
     * @param book   图书信息
     * @return 更新后的图书信息
     */
    @Transactional
    public BookInfoResponse updateBook(Long bookId, Book book) {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        // 更新允许修改的字段
        if (book.getTitle() != null) {
            existingBook.setTitle(book.getTitle());
        }
        if (book.getAuthor() != null) {
            existingBook.setAuthor(book.getAuthor());
        }
        if (book.getPublisher() != null) {
            existingBook.setPublisher(book.getPublisher());
        }
        if (book.getPublishDate() != null) {
            existingBook.setPublishDate(book.getPublishDate());
        }
        if (book.getCategory() != null) {
            existingBook.setCategory(book.getCategory());
        }
        if (book.getPrice() != null) {
            existingBook.setPrice(book.getPrice());
        }
        if (book.getCoverUrl() != null) {
            existingBook.setCoverUrl(book.getCoverUrl());
        }
        if (book.getDescription() != null) {
            existingBook.setDescription(book.getDescription());
        }
        if (book.getLocation() != null) {
            existingBook.setLocation(book.getLocation());
        }
        if (book.getTotalStock() != null) {
            // 更新总库存时，同步更新可借数量
            int diff = book.getTotalStock() - existingBook.getTotalStock();
            existingBook.setTotalStock(book.getTotalStock());
            int newAvailableStock = existingBook.getAvailableStock() + diff;
            if (newAvailableStock < 0) {
                throw new RuntimeException("可借数量不能为负数");
            }
            existingBook.setAvailableStock(newAvailableStock);
        }

        existingBook = bookRepository.save(existingBook);
        return convertToResponse(existingBook);
    }

    /**
     * 删除图书
     *
     * @param bookId 图书ID
     */
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        // 检查是否有借阅记录
        if (book.getAvailableStock() < book.getTotalStock()) {
            throw new RuntimeException("该图书仍有借阅记录，无法删除");
        }

        bookRepository.delete(book);
    }

    /**
     * 搜索图书
     *
     * @param request 搜索请求
     * @return 图书列表
     */
    public Page<BookInfoResponse> searchBooks(SearchBookRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNum() - 1, request.getPageSize());

        // 转换状态字符串为枚举
        Book.BookStatus status = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                status = Book.BookStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的图书状态");
            }
        }

        Page<Book> books = bookRepository.searchBooks(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getCategory(),
                request.getPublisher(),
                status,
                pageable
        );

        return books.map(this::convertToResponse);
    }

    /**
     * 获取可借阅图书列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 图书列表
     */
    public Page<BookInfoResponse> getAvailableBooks(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Book> books = bookRepository.findAvailableBooks(pageable);
        return books.map(this::convertToResponse);
    }

    /**
     * 获取热门图书
     *
     * @param limit 数量限制
     * @return 图书列表
     */
    public List<BookInfoResponse> getPopularBooks(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Book> books = bookRepository.findPopularBooks(pageable);
        return books.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * 增加图书可借数量
     *
     * @param bookId 图书ID
     */
    @Transactional
    public void increaseAvailableStock(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        if (book.getAvailableStock() >= book.getTotalStock()) {
            throw new RuntimeException("可借数量不能超过总库存");
        }

        book.setAvailableStock(book.getAvailableStock() + 1);

        // 如果之前没有可借数量，更新状态为可借
        if (book.getAvailableStock() == 1 && book.getStatus() != Book.BookStatus.AVAILABLE) {
            book.setStatus(Book.BookStatus.AVAILABLE);
        }

        bookRepository.save(book);
    }

    /**
     * 减少图书可借数量
     *
     * @param bookId 图书ID
     */
    @Transactional
    public void decreaseAvailableStock(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        if (book.getAvailableStock() <= 0) {
            throw new RuntimeException("该图书暂无可借数量");
        }

        book.setAvailableStock(book.getAvailableStock() - 1);

        // 如果没有可借数量，更新状态
        if (book.getAvailableStock() == 0) {
            book.setStatus(Book.BookStatus.BORROWED);
        }

        bookRepository.save(book);
    }

    /**
     * 检查图书是否可借
     *
     * @param bookId 图书ID
     * @return 是否可借
     */
    public boolean isAvailable(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        return book.getStatus() == Book.BookStatus.AVAILABLE && book.getAvailableStock() > 0;
    }

    /**
     * 将Book实体转换为BookInfoResponse
     *
     * @param book 图书实体
     * @return 图书信息响应
     */
    private BookInfoResponse convertToResponse(Book book) {
        return BookInfoResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishDate())
                .category(book.getCategory())
                .price(book.getPrice())
                .totalStock(book.getTotalStock())
                .availableStock(book.getAvailableStock())
                .coverUrl(book.getCoverUrl())
                .description(book.getDescription())
                .location(book.getLocation())
                .status(book.getStatus().name())
                .statusDescription(book.getStatus().getDescription())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
