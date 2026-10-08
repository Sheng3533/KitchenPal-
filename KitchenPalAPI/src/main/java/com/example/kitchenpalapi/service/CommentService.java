package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.Comment;
import com.example.kitchenpalapi.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> listByRecipe(Long recipeId) {
        return commentRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId);
    }

    public long countByRecipe(Long recipeId) {
        return commentRepository.countByRecipeId(recipeId);
    }

    public Comment add(Comment comment) {
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new RuntimeException("评论内容不能为空");
        }
        // 新增时清掉客户端占位 id，避免 JPA 走 merge 当成更新
        comment.setId(null);
        return commentRepository.save(comment);
    }

    public void delete(Long id, Long userId) {
        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        if (!c.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }
        commentRepository.delete(c);
    }
}
