package lion.mode.tradebot_backend.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class BacktestParameterHelper {

    public static String parametersToJson(Map<String, Object> parameters) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(parameters);
        } catch (Exception e) {
            return "{}";
        }
    }

    public static Map<String, Object> parametersFromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public static <T> T getParameter(Map<String, Object> parameters,
                                     String key, Class<T> type, T defaultValue) {
        Object value = parameters.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return defaultValue;
    }

    public static boolean validateParameters(String indicator, Map<String, Object> parameters) {
        switch (indicator.toUpperCase()) {
            case "RSI":
                return parameters.containsKey("period") &&
                        parameters.containsKey("upperLimit") &&
                        parameters.containsKey("lowerLimit");

            case "MACD":
                return parameters.containsKey("fastPeriod") &&
                        parameters.containsKey("slowPeriod") &&
                        parameters.containsKey("signalPeriod");

            case "BOLLINGER_BANDS":
                return parameters.containsKey("period") &&
                        parameters.containsKey("standardDeviations");

            case "SMA":
            case "EMA":
                return parameters.containsKey("period");

            case "STOCHASTIC":
                return parameters.containsKey("kPeriod") &&
                        parameters.containsKey("dPeriod");

            default:
                return true;
        }
    }
}