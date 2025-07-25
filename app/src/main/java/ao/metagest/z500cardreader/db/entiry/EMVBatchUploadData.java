package ao.metagest.z500cardreader.db.entiry;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

@Entity(nameInDb = "EMVBatchUploadData")
public class EMVBatchUploadData {
    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "TAG82")
    private String tag82;
    @Property(nameInDb = "TAG9F36")
    private String tag9F36;
    @Property(nameInDb = "TAG9F07")
    private String tag9F07;
    @Property(nameInDb = "TAG9F27")
    private String tag9F27;
    @Property(nameInDb = "TAG8E")
    private String tag8E;
    @Property(nameInDb = "TAG9F34")
    private String tag9F34;
    @Property(nameInDb = "TAG9F1E")
    private String tag9F1E;
    @Property(nameInDb = "TAG9F0D")
    private String tag9F0D;
    @Property(nameInDb = "TAG9F0E")
    private String tag9F0E;
    @Property(nameInDb = "TAG9F0F")
    private String tag9F0F;
    @Property(nameInDb = "TAG9F10")
    private String tag9F10;
    @Property(nameInDb = "TAGDF31")
    private String tagDF31;
    @Property(nameInDb = "TAG9F33")
    private String tag9F33;
    @Property(nameInDb = "TAG9F35")
    private String tag9F35;
    @Property(nameInDb = "TAG95")
    private String tag95;
    @Property(nameInDb = "TAG9B")
    private String tag9B;
    @Property(nameInDb = "TAG9F26")
    private String tag9F26;
    @Property(nameInDb = "TAG9F37")
    private String tag9F37;
    @Property(nameInDb = "TAG9F01")
    private String tag9F01;
    @Property(nameInDb = "TAG9F02")
    private String tag9F02;
    @Property(nameInDb = "TAG9F03")
    private String tag9F03;
    @Property(nameInDb = "TAG5F25")
    private String tag5F25;
    @Property(nameInDb = "TAG5F24")
    private String tag5F24;
    @Property(nameInDb = "TAG5A")
    private String tag5A;
    @Property(nameInDb = "TAG5F34")
    private String tag5F34;
    @Property(nameInDb = "TAG5F28")
    private String tag5F28;
    @Property(nameInDb = "TAG9F15")
    private String tag9F15;
    @Property(nameInDb = "TAG9F16")
    private String tag9F16;
    @Property(nameInDb = "TAG9F1A")
    private String tag9F1A;
    @Property(nameInDb = "TAG9F1C")
    private String tag9F1C;
    @Property(nameInDb = "TAG57")
    private String tag57;
    @Property(nameInDb = "TAG81")
    private String tag81;
    @Property(nameInDb = "TAG5F2A")
    private String tag5F2A;
    @Property(nameInDb = "TAG9A")
    private String tag9A;
    @Property(nameInDb = "TAG9F21")
    private String tag9F21;
    @Property(nameInDb = "TAG9C")
    private String tag9C;
    @Property(nameInDb = "TAG9F24")
    private String tag9F24;
    @Property(nameInDb = "TAG9F19")
    private String tag9F19;
    @Property(nameInDb = "TAG9F06")
    private String tag9F06;
    @Property(nameInDb = "TAG8A")
    private String tag8A;
    @Property(nameInDb = "TAG9F39")
    private String tag9F39;
    @Property(nameInDb = "TIME_STAMP")
    private Date timeStamp;
    @Generated(hash = 1155798749)
    public EMVBatchUploadData(Long id, String tag82, String tag9F36, String tag9F07,
            String tag9F27, String tag8E, String tag9F34, String tag9F1E,
            String tag9F0D, String tag9F0E, String tag9F0F, String tag9F10,
            String tagDF31, String tag9F33, String tag9F35, String tag95,
            String tag9B, String tag9F26, String tag9F37, String tag9F01,
            String tag9F02, String tag9F03, String tag5F25, String tag5F24,
            String tag5A, String tag5F34, String tag5F28, String tag9F15,
            String tag9F16, String tag9F1A, String tag9F1C, String tag57,
            String tag81, String tag5F2A, String tag9A, String tag9F21,
            String tag9C, String tag9F24, String tag9F19, String tag9F06,
            String tag8A, String tag9F39, Date timeStamp) {
        this.id = id;
        this.tag82 = tag82;
        this.tag9F36 = tag9F36;
        this.tag9F07 = tag9F07;
        this.tag9F27 = tag9F27;
        this.tag8E = tag8E;
        this.tag9F34 = tag9F34;
        this.tag9F1E = tag9F1E;
        this.tag9F0D = tag9F0D;
        this.tag9F0E = tag9F0E;
        this.tag9F0F = tag9F0F;
        this.tag9F10 = tag9F10;
        this.tagDF31 = tagDF31;
        this.tag9F33 = tag9F33;
        this.tag9F35 = tag9F35;
        this.tag95 = tag95;
        this.tag9B = tag9B;
        this.tag9F26 = tag9F26;
        this.tag9F37 = tag9F37;
        this.tag9F01 = tag9F01;
        this.tag9F02 = tag9F02;
        this.tag9F03 = tag9F03;
        this.tag5F25 = tag5F25;
        this.tag5F24 = tag5F24;
        this.tag5A = tag5A;
        this.tag5F34 = tag5F34;
        this.tag5F28 = tag5F28;
        this.tag9F15 = tag9F15;
        this.tag9F16 = tag9F16;
        this.tag9F1A = tag9F1A;
        this.tag9F1C = tag9F1C;
        this.tag57 = tag57;
        this.tag81 = tag81;
        this.tag5F2A = tag5F2A;
        this.tag9A = tag9A;
        this.tag9F21 = tag9F21;
        this.tag9C = tag9C;
        this.tag9F24 = tag9F24;
        this.tag9F19 = tag9F19;
        this.tag9F06 = tag9F06;
        this.tag8A = tag8A;
        this.tag9F39 = tag9F39;
        this.timeStamp = timeStamp;
    }
    @Generated(hash = 790298967)
    public EMVBatchUploadData() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTag82() {
        return this.tag82;
    }
    public void setTag82(String tag82) {
        this.tag82 = tag82;
    }
    public String getTag9F36() {
        return this.tag9F36;
    }
    public void setTag9F36(String tag9F36) {
        this.tag9F36 = tag9F36;
    }
    public String getTag9F07() {
        return this.tag9F07;
    }
    public void setTag9F07(String tag9F07) {
        this.tag9F07 = tag9F07;
    }
    public String getTag9F27() {
        return this.tag9F27;
    }
    public void setTag9F27(String tag9F27) {
        this.tag9F27 = tag9F27;
    }
    public String getTag8E() {
        return this.tag8E;
    }
    public void setTag8E(String tag8E) {
        this.tag8E = tag8E;
    }
    public String getTag9F34() {
        return this.tag9F34;
    }
    public void setTag9F34(String tag9F34) {
        this.tag9F34 = tag9F34;
    }
    public String getTag9F1E() {
        return this.tag9F1E;
    }
    public void setTag9F1E(String tag9F1E) {
        this.tag9F1E = tag9F1E;
    }
    public String getTag9F0D() {
        return this.tag9F0D;
    }
    public void setTag9F0D(String tag9F0D) {
        this.tag9F0D = tag9F0D;
    }
    public String getTag9F0E() {
        return this.tag9F0E;
    }
    public void setTag9F0E(String tag9F0E) {
        this.tag9F0E = tag9F0E;
    }
    public String getTag9F0F() {
        return this.tag9F0F;
    }
    public void setTag9F0F(String tag9F0F) {
        this.tag9F0F = tag9F0F;
    }
    public String getTag9F10() {
        return this.tag9F10;
    }
    public void setTag9F10(String tag9F10) {
        this.tag9F10 = tag9F10;
    }
    public String getTagDF31() {
        return this.tagDF31;
    }
    public void setTagDF31(String tagDF31) {
        this.tagDF31 = tagDF31;
    }
    public String getTag9F33() {
        return this.tag9F33;
    }
    public void setTag9F33(String tag9F33) {
        this.tag9F33 = tag9F33;
    }
    public String getTag9F35() {
        return this.tag9F35;
    }
    public void setTag9F35(String tag9F35) {
        this.tag9F35 = tag9F35;
    }
    public String getTag95() {
        return this.tag95;
    }
    public void setTag95(String tag95) {
        this.tag95 = tag95;
    }
    public String getTag9B() {
        return this.tag9B;
    }
    public void setTag9B(String tag9B) {
        this.tag9B = tag9B;
    }
    public String getTag9F26() {
        return this.tag9F26;
    }
    public void setTag9F26(String tag9F26) {
        this.tag9F26 = tag9F26;
    }
    public String getTag9F37() {
        return this.tag9F37;
    }
    public void setTag9F37(String tag9F37) {
        this.tag9F37 = tag9F37;
    }
    public String getTag9F01() {
        return this.tag9F01;
    }
    public void setTag9F01(String tag9F01) {
        this.tag9F01 = tag9F01;
    }
    public String getTag9F02() {
        return this.tag9F02;
    }
    public void setTag9F02(String tag9F02) {
        this.tag9F02 = tag9F02;
    }
    public String getTag9F03() {
        return this.tag9F03;
    }
    public void setTag9F03(String tag9F03) {
        this.tag9F03 = tag9F03;
    }
    public String getTag5F25() {
        return this.tag5F25;
    }
    public void setTag5F25(String tag5F25) {
        this.tag5F25 = tag5F25;
    }
    public String getTag5F24() {
        return this.tag5F24;
    }
    public void setTag5F24(String tag5F24) {
        this.tag5F24 = tag5F24;
    }
    public String getTag5A() {
        return this.tag5A;
    }
    public void setTag5A(String tag5A) {
        this.tag5A = tag5A;
    }
    public String getTag5F34() {
        return this.tag5F34;
    }
    public void setTag5F34(String tag5F34) {
        this.tag5F34 = tag5F34;
    }
    public String getTag5F28() {
        return this.tag5F28;
    }
    public void setTag5F28(String tag5F28) {
        this.tag5F28 = tag5F28;
    }
    public String getTag9F15() {
        return this.tag9F15;
    }
    public void setTag9F15(String tag9F15) {
        this.tag9F15 = tag9F15;
    }
    public String getTag9F16() {
        return this.tag9F16;
    }
    public void setTag9F16(String tag9F16) {
        this.tag9F16 = tag9F16;
    }
    public String getTag9F1A() {
        return this.tag9F1A;
    }
    public void setTag9F1A(String tag9F1A) {
        this.tag9F1A = tag9F1A;
    }
    public String getTag9F1C() {
        return this.tag9F1C;
    }
    public void setTag9F1C(String tag9F1C) {
        this.tag9F1C = tag9F1C;
    }
    public String getTag57() {
        return this.tag57;
    }
    public void setTag57(String tag57) {
        this.tag57 = tag57;
    }
    public String getTag81() {
        return this.tag81;
    }
    public void setTag81(String tag81) {
        this.tag81 = tag81;
    }
    public String getTag5F2A() {
        return this.tag5F2A;
    }
    public void setTag5F2A(String tag5F2A) {
        this.tag5F2A = tag5F2A;
    }
    public String getTag9A() {
        return this.tag9A;
    }
    public void setTag9A(String tag9A) {
        this.tag9A = tag9A;
    }
    public String getTag9F21() {
        return this.tag9F21;
    }
    public void setTag9F21(String tag9F21) {
        this.tag9F21 = tag9F21;
    }
    public String getTag9C() {
        return this.tag9C;
    }
    public void setTag9C(String tag9C) {
        this.tag9C = tag9C;
    }
    public String getTag9F24() {
        return this.tag9F24;
    }
    public void setTag9F24(String tag9F24) {
        this.tag9F24 = tag9F24;
    }
    public String getTag9F19() {
        return this.tag9F19;
    }
    public void setTag9F19(String tag9F19) {
        this.tag9F19 = tag9F19;
    }
    public String getTag9F06() {
        return this.tag9F06;
    }
    public void setTag9F06(String tag9F06) {
        this.tag9F06 = tag9F06;
    }
    public String getTag8A() {
        return this.tag8A;
    }
    public void setTag8A(String tag8A) {
        this.tag8A = tag8A;
    }
    public String getTag9F39() {
        return this.tag9F39;
    }
    public void setTag9F39(String tag9F39) {
        this.tag9F39 = tag9F39;
    }
    public Date getTimeStamp() {
        return this.timeStamp;
    }
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

}
