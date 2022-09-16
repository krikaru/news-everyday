package com.example.newsapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Data
@ToString(exclude = {"bannedUser", "moderatorUser"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class LockAccount {
    @Id
    @SequenceGenerator(
            name = "lock_account_id_sequence",
            sequenceName = "lock_account_id_sequence"
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lock_account_id_sequence")
    @JsonView(Views.FullLockAccount.class)
    private Long id;

    @NotBlank(message = "Причина блокировки не должна быть пуста!")
    @Length(message = "Длина текста причины должна быть не меньше 5 и не больше 500 символов.", max = 500, min = 5)
    @JsonView(Views.ShortLockAccount.class)
    private String reason;

    @JsonView(Views.FullLockAccount.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    private LocalDateTime banDate;

    @OneToOne
    @JoinColumn(name = "banned_user_id", unique = true, nullable = false)
    @JsonView(Views.ShortLockAccount.class)
    private AppUser bannedUser;

    @ManyToOne
    @JoinColumn(name = "moderator_user_id", nullable = false)
    @JsonView(Views.FullLockAccount.class)
    private AppUser moderatorUser;


}
