package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.Comment;
import com.example.kitchenpalapi.entity.Recipe;
import com.example.kitchenpalapi.entity.RecipeRating;
import com.example.kitchenpalapi.entity.User;
import com.example.kitchenpalapi.service.CommentService;
import com.example.kitchenpalapi.service.RecipeService;
import com.example.kitchenpalapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService;

    /** 取用户展示名（昵称优先，其次脱敏手机号） */
    private String displayName(Long userId) {
        try {
            User u = userService.getUserById(userId);
            if (u == null) return "用户";
            if (u.getNickname() != null && !u.getNickname().isBlank()) return u.getNickname();
            String phone = u.getPhone();
            if (phone != null && phone.length() >= 11) {
                return phone.substring(0, 3) + "****" + phone.substring(7);
            }
            return "用户";
        } catch (Exception e) {
            return "用户";
        }
    }

    /** 某菜谱的评论列表 */
    @GetMapping("/comment/recipe/{recipeId}")
    public ApiResponse<List<Comment>> listComments(@PathVariable Long recipeId) {
        return ApiResponse.success(commentService.listByRecipe(recipeId));
    }

    /** 发表评论（需登录：userId 取自 JWT） */
    @PostMapping("/comment")
    public ApiResponse<Comment> addComment(@RequestBody Comment comment, HttpServletRequest request) {
        Long uid = (Long) request.getAttribute("userId");
        if (uid == null) return ApiResponse.error(401, "未登录");
        comment.setUserId(uid);
        if (comment.getUserName() == null || comment.getUserName().isBlank()) {
            comment.setUserName(displayName(uid));
        }
        Comment saved = commentService.add(comment);
        return ApiResponse.success("评论成功", saved);
    }

    /** 删除自己的评论 */
    @DeleteMapping("/comment/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long uid = (Long) request.getAttribute("userId");
        if (uid == null) return ApiResponse.error(401, "未登录");
        commentService.delete(id, uid);
        return ApiResponse.success(null);
    }

    /** 用户对菜谱评分（1~5 星，可带一句评价），后端重算平均分 */
    @PostMapping("/recipe/{id}/rate")
    public ApiResponse<Recipe> rateRecipe(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body,
                                          HttpServletRequest request) {
        Long uid = (Long) request.getAttribute("userId");
        if (uid == null) return ApiResponse.error(401, "未登录");
        Object ratingObj = body.get("rating");
        if (ratingObj == null) return ApiResponse.error("缺少评分");
        int rating = ((Number) ratingObj).intValue();
        String userName = body.get("userName") != null
                ? body.get("userName").toString() : displayName(uid);
        String comment = body.get("comment") != null ? body.get("comment").toString() : null;
        Recipe updated = recipeService.rateRecipe(id, uid, userName, rating, comment);
        return ApiResponse.success("评分成功", updated);
    }

    /** 某菜谱的评分列表 */
    @GetMapping("/recipe/{id}/ratings")
    public ApiResponse<List<RecipeRating>> listRatings(@PathVariable Long id) {
        return ApiResponse.success(recipeService.getRatings(id));
    }
}
