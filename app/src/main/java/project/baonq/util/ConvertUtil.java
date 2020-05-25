package project.baonq.util;

import java.text.DecimalFormat;

import project.baonq.enumeration.Currency;
import project.baonq.menu.R;

public class ConvertUtil {

    public static String convertCashFormat(double number) {
        //format number
        DecimalFormat decimalFormat = new DecimalFormat("#,###,###,###");
        return decimalFormat.format(number);
    }

    public static String convertCurrency(String currency) {
        switch (currency) {
            case "VND":
                return "đ";
            case "Dollar":
                return "$";
            case "Euro":
                return "€";
            default:
                return "";
        }
    }

    public static String formatMoney(double amount) {
        return (amount < 0 ? "-" : "") + ConvertUtil.convertCashFormat(Math.abs(amount));
    }

    public static int mapIcon(String value) {
        switch (value) {
            case "Lãi ngân hàng":
                return R.drawable.ic_report_icome_atm_24dp;
            case "Khác":
                return R.drawable.ic_report_else_black_24dp;
            case "Được tặng":
                return R.drawable.ic_report_gift_black_24dp;
            case "Tiền chuyển đến":
                return R.drawable.ic_report_receive_24dp;
            case "Lương":
                return R.drawable.ic_report_money_black_24dp;
            case "Ăn uống":
                return R.drawable.ic_report_eat_black_24dp;
            case "Hóa đơn":
                return R.drawable.ic_bill_black_24dp;
            case "Mua sắm":
                return R.drawable.ic_report_shopping_cart_black_24dp;
            case "Di chuyển":
                return R.drawable.ic_report_shipping_black_24dp;
            default:
                return R.drawable.wallet;
        }
    }
}
