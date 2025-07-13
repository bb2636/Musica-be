package com.example.musica_be.domain.settlement;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "instructor_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name= "class_id" , nullable = false)
  private Classes classes;

  private Long total_amount;
  private Long commission_rate;
  private Long net_amount;

  private String settlement_month;
  private LocalDateTime settled_at;
}
