package com.master.Restful_Blog_Api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing /// Without it, @CreatedDate and @LastModifiedDate won't work that use it in (Post, User, Comment)
public class RestfulBlogApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestfulBlogApiApplication.class, args);
	}

}
