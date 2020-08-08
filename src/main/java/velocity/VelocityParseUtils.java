package velocity;

import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.ASTSetDirective;
import org.apache.velocity.runtime.parser.node.ParserVisitor;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.BaseVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author chabeimao
 * @date 2020-07-09
 */
@Slf4j
public class VelocityParseUtils {

    private static final Set<String> KEYWORD = new HashSet<>();

    static {
        KEYWORD.add("foreach");
        KEYWORD.add("env");
    }

    private static Pattern NAME_PATTERN = Pattern.compile("\\$!?\\{?([a-zA-Z]\\w*)");

    public static List<String> getVelocityParam(String sql) {
        RuntimeInstance ri = new RuntimeInstance();
        SimpleNode node = null;
        try {
            node = ri.parse(sql, "templateName");
        } catch (ParseException e) {
            String msg = e.getMessage();
            System.out.println("velocity解析异常:" + msg);
        }

        Set<String> varSet = new HashSet<>();
        Set<String> excludeVarSet = new HashSet<>();
        ParserVisitor visitor = new BaseVisitor() {
            // exclude定义的局部变量
            @Override
            public Object visit(ASTDirective node, Object data) {
                String excludeVariable = node.jjtGetChild(0).literal();
                excludeVarSet.add(excludeVariable);
                return super.visit(node, data);
            }

            // exclude定义的局部变量
            @Override
            public Object visit(ASTSetDirective node, Object data) {
                //                node.getLeftHandSide();
                String excludeVariable = node.jjtGetChild(0).literal();
                excludeVarSet.add(excludeVariable);
                return super.visit(node, data);
            }

            @Override
            public Object visit(final ASTReference node, final Object data) {
                varSet.add(node.literal());
                return super.visit(node, data);
            }
        };

        try {
            visitor.visit(node, null);
        } catch (Exception ex) {
            // 例子：
            // String sql = "SELECT '#pt' AS pt, '#hr' AS hr;";
            // 上述语句会抛出NullPointerException，原因是简单SQL需要{},被误认为velocity语句
            System.out.println("velocity语法错误");
        }

        // 删除局部变量
        varSet.removeAll(excludeVarSet);

        // 得到变量名+删除关键字
        // 其中如果变量调用了方法也会被转换，如$pt.split(",") => pt
        // 这样的话二级变量也会被过滤如$map.key
        Set<String> finalVarSet = varSet.stream()
            .map(r -> NAME_PATTERN.matcher(r))
            .filter(Matcher::find)
            .map(m -> m.group(1))
            .filter(var -> !KEYWORD.contains(var))
            .collect(Collectors.toSet()); // 可能出现$var, ${var}的情况，用Set过滤

        return new ArrayList<>(finalVarSet);
    }


}
