package com.cheetahnet.ping;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PingApplication{




	public static void main(String[] args) {
		SpringApplication.run(PingApplication.class, args);
	}


	@RequestMapping(value = "/")
	public String hello() {
		return "Hello World from Tomcat";

	}


}
