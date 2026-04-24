package com.internship.backend_assignment.repository;

import com.internship.backend_assignment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment , Long> {
}
