package velocity;

import java.util.List;

/**
 * @author chabeimao
 * @date 2020-07-09
 */
public class VelocityLearm {

    public static void main(String[] args) {
        String sql = "SELECT timeGranularity(`time`, m) AS __timestamp,\n"
            + "       vendor AS vendor,\n"
            + "       account AS account,\n"
            + "       SUM(`push_count`) AS push_count_sum,\n"
            + "       sum(push_succ_count) AS push_succ_count,\n"
            + "       sum(ten_seconds_count) AS ten_seconds_count\n"
            + "FROM druid_sms_account_stat\n"
            + "WHERE time >= \"${start_time}\"\n"
            + "  AND time <= \"${end_time}\"\n"
            + "#if($group_id) and group_id = ${group_id} #end\n"
            + "#if($sms_type) and `type` = ${sms_type} #end \n"
            + "#if($sign_type) and sign_type = ${sign_type} #end\n"
            + "GROUP BY vendor,\n"
            + "         account,\n"
            + "         timeGranularity(`time`, m);";
        List<String> paramList = VelocityParseUtils.getVelocityParam(sql);
        System.out.println(paramList);
    }
}
