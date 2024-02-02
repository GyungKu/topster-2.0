package com.sparta.topster.domain.user.service.mail;

import com.sparta.topster.global.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender emailSender; // MailConfig에서 등록해둔 Bean을 autowired하여 사용하기

    private String ePw; // 사용자가 메일로 받을 인증번호

    private final RedisUtil redisUtil;

    private static final int MAX_VERIFICATIONS_PER_DAY = 10;

    @Value("${mail.id}")
    private String from;

    // 메일 내용 작성
    @Override
    public MimeMessage creatMessage(String email) throws MessagingException, UnsupportedEncodingException {
        System.out.println("메일받을 사용자" + email);
        System.out.println("인증번호" + ePw);

        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, email); // 메일 받을 사용자
        message.setSubject("[Topster2.0] 회원가입을 위한 이메일 인증코드 입니다"); // 이메일 제목

        String msgg = "";
        msgg += "<h1>안녕하세요</h1>";
        msgg += "<h1>음악 공유 플랫폼 Topster2.0 입니다</h1>";
        msgg += "<br>";
        msgg += "<p>아래 인증코드를 암호변경 페이지에 입력해주세요</p>";
        msgg += "<br>";
        msgg += "<br>";
        msgg += "<div align='center' style='border:1px solid black'>";
        msgg += "<h3 style='color:blue'>회원가입 인증코드 입니다</h3>";
        msgg += "<div style='font-size:130%'>";
        msgg += "<strong>" + ePw + "</strong></div><br/>" ; // 메일에 인증번호 ePw 넣기
        msgg += "</div>";
        // msgg += "<img src=../resources/static/image/emailfooter.jpg />"; // footer image

        message.setText(msgg, "utf-8", "html"); // 메일 내용, charset타입, subtype
        // 보내는 사람의 이메일 주소, 보내는 사람 이름
        message.setFrom(new InternetAddress(from, "Topster"));
        System.out.println("********creatMessage 함수에서 생성된 msgg 메시지********" + msgg);

        System.out.println("********creatMessage 함수에서 생성된 리턴 메시지********" + message);


        return message;
    }

    // 랜덤 인증코드 생성
    @Override
    public String createKey() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        String key = random.ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
        System.out.println("생성된 랜덤 인증코드"+ key);
        return key;
    }

    // 메일 발송
    // sendSimpleMessage 의 매개변수 to는 이메일 주소가 되고,
    // MimeMessage 객체 안에 내가 전송할 메일의 내용을 담는다
    // bean으로 등록해둔 javaMail 객체를 사용하여 이메일을 발송한다
    @Override
    public String sendSimpleMessage(String email) throws Exception {
        String verificationCountKey = "verificationCount:" + email;
        //일 10회
        Long count = redisUtil.increment(verificationCountKey,1);
        if(count > MAX_VERIFICATIONS_PER_DAY){
            throw new RuntimeException("일 인증 횟수 초과");
        }
        redisUtil.setExpire(verificationCountKey, getSecondsUntilMidnight());

        ePw = createKey(); // 랜덤 인증코드 생성

        redisUtil.setDataExpire(email,ePw,60*5L); //5분

        System.out.println("********생성된 랜덤 인증코드******** => " + ePw);

        MimeMessage message = creatMessage(email); // "to" 로 메일 발송

        System.out.println("********생성된 메시지******** => " + message);


        try {
            emailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }

        return ePw;
    }

    private long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        //00:00시
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return ChronoUnit.SECONDS.between(now, midnight);
    }

}
