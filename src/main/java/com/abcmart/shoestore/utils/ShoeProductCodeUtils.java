package com.abcmart.shoestore.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class ShoeProductCodeUtils {

    private static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Long과 LocalDate를 하이픈(-)으로 이어 문자열로 반환한다.
     * 예: 123456789L, 2025-06-08 -> "123456789-20250608"
     *
     * @param shoeCode Long 타입 숫자
     * @param stockedDate LocalDate 타입 날짜
     * @return 결합된 문자열
     */
    public static String generate(Long shoeCode, LocalDate stockedDate) {

        if (Objects.isNull(shoeCode) || Objects.isNull(stockedDate)) {
            throw new IllegalArgumentException("number와 date는 null일 수 없습니다.");
        }

        return shoeCode + "-" + stockedDate.format(YYYYMMDD_FORMATTER);
    }

    /**
     * "123456789-20250608" 형식의 문자열을 Long과 LocalDate로 분리
     *
     * @param shoeProductCode 결합된 문자열
     * @return ParsedResult 객체
     * @throws IllegalArgumentException 형식이 잘못된 경우
     */
    public static ParsedResult parse(String shoeProductCode) {

        if (shoeProductCode == null || !shoeProductCode.contains("-")) {
            throw new IllegalArgumentException("잘못된 형식입니다. '숫자-yyyyMMdd' 형식이어야 합니다.");
        }

        String[] parts = shoeProductCode.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("하이픈이 하나만 있어야 합니다.");
        }

        try {

            Long shoeCode = Long.parseLong(parts[0]);
            LocalDate stockedDate = LocalDate.parse(parts[1], YYYYMMDD_FORMATTER);
            return new ParsedResult(shoeCode, stockedDate);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자 부분이 Long 타입이 아닙니다: " + parts[0], e);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 부분이 yyyyMMdd 형식이 아닙니다: " + parts[1], e);
        }
    }

    public record ParsedResult (Long shoeCode, LocalDate stockedDate) {}
}
