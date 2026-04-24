package com.internship.backend_assignment.repository;

import com.internship.backend_assignment.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post , Long> {
}
