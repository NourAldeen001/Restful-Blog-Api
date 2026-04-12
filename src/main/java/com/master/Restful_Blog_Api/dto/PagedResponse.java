package com.master.Restful_Blog_Api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize; // posts per page
    private long totalElements; // Total posts in database
    private int totalPages; // Total pages
    private boolean first; // Is first page
    private boolean last; // Is last page
    private boolean empty; // Is result empty
}
