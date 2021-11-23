package cn.ft.pojo;

import cn.ft.orm.BasePojo;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author ckn
 * @date 2021/11/22
 */
public class TestPojo extends BasePojo<User> {
    private TestPojo() {
    }

    public static TestPojo create() {
        TestPojo testPojo = new TestPojo();
        testPojo.tableName = "test_user";
        testPojo.className = "User";
        testPojo.init();
        return testPojo;
    }

    public TestPojo id(Long... ids) {
        this.conditions.put("id", Arrays.stream(ids).distinct().collect(Collectors.toList()));
        return this;
    }

    public TestPojo email(String... emails) {
        this.conditions.put("email", Arrays.stream(emails).distinct().collect(Collectors.toList()));
        return this;
    }

    public TestPojo password(String... passwords) {
        this.conditions.put("password", Arrays.stream(passwords).distinct().collect(Collectors.toList()));
        return this;
    }

    public TestPojo name(String... names) {
        this.conditions.put("name", Arrays.stream(names).distinct().collect(Collectors.toList()));
        return this;
    }

    public TestPojo didP(String... didPs) {
        this.conditions.put("didP", Arrays.stream(didPs).distinct().collect(Collectors.toList()));
        return this;
    }
}
