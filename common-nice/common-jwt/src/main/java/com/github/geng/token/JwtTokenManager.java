package com.github.geng.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author geng
 */
@Component
@Slf4j
public class JwtTokenManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 获取用户登陆名
     * @param token token
     * @return
     */
    public String getLoginNameFromToken(String token) {
        String username;
        try {
            final Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
            log.error("根据token获取用户名失败,token:{}",token);
        }
        return username;
    }

    /**
     * 获取token创建时间
     * @param token token
     * @return
     */
    public Date getCreatedDateFromToken(String token) {
        Date created;
        try {
            final Claims claims = getClaimsFromToken(token);
            created = new Date((Long) claims.get(CLAIM_KEY_CREATED));
        } catch (Exception e) {
            created = null;
            log.error("根据token获取创建时间失败,token:{}",token);
        }
        return created;
    }

    /**
     * 获取token过期日期
     * @param token token
     * @return
     */
    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
            log.error("根据token获取过期时间失败,token:{}",token);
        }
        return expiration;
    }

    /**
     * 判断token是否过期
     * @return true 未过期 | false 过期
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 获取token
     * @param loginName 用户登录名
     * @return
     */
    public String generateToken(String loginName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, loginName);
        claims.put(CLAIM_KEY_CREATED, new Date());
        log.debug("根据用户名:{}获取token", loginName);
        return generateToken(claims);
    }

    /**
     * 判断token是否需要刷新
     * @param token token
     * @param lastPasswordReset 最近一次token产生时间
     * @return
     */
    public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
        final Date created = getCreatedDateFromToken(token);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
                && !isTokenExpired(token);
    }

    /**
     * 刷新token
     * @param token 旧token
     * @return
     */
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token);
            claims.put(CLAIM_KEY_CREATED, new Date());
            refreshedToken = generateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
            log.error("根据旧token刷新失败,旧token:{}",token);
        }
        return refreshedToken;
    }

    /**
     * token验证
     * @param token 系统产生的token
     * @param loginName 用户登录名
     * @param passwordResetTime 最近一次token产生时间
     * @return
     */
    public Boolean validateToken(String token, String loginName, long passwordResetTime) {
        final String oldLoginName = getLoginNameFromToken(token);
        final Date created = getCreatedDateFromToken(token);

        return (
                oldLoginName.equals(loginName)
                        && !isTokenExpired(token)
                        && !isCreatedBeforeLastPasswordReset(created, new Date(passwordResetTime)));
    }

    //=======================================================
    //private methods
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析token异常",e);
            claims = null;
        }
        return claims;
    }

    private String generateToken(Map<String, Object> claims) {
        log.debug("开始产生token");
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
                .compact();
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + jwtConfig.getExpiration() * 1000);
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

}
