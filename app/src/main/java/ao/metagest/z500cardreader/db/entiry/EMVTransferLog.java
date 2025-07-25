package ao.metagest.z500cardreader.db.entiry;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

@Entity(nameInDb = "EMVTransferLog")
public class EMVTransferLog {
    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "TAG9A")
    private String tag9A;
    @Property(nameInDb = "TAG9F21")
    private String tag9F21;
    @Property(nameInDb = "TAG5F2A")
    private String tag5F2A;
    @Property(nameInDb = "TAG9F02")
    private String tag9F02;
    @Property(nameInDb = "TAG9F4E")
    private String tag9F4E;
    @Property(nameInDb = "TAG9F36")
    private String tag9F36;
    @Property(nameInDb = "TIME_STAMP")
    private Date timeStamp;

    @Generated(hash = 2108663378)
    public EMVTransferLog(Long id, String tag9A, String tag9F21, String tag5F2A, String tag9F02,
                          String tag9F4E, String tag9F36, Date timeStamp) {
        this.id = id;
        this.tag9A = tag9A;
        this.tag9F21 = tag9F21;
        this.tag5F2A = tag5F2A;
        this.tag9F02 = tag9F02;
        this.tag9F4E = tag9F4E;
        this.tag9F36 = tag9F36;
        this.timeStamp = timeStamp;
    }

    @Generated(hash = 1099868722)
    public EMVTransferLog() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTag5F2A() {
        return this.tag5F2A;
    }

    public void setTag5F2A(String tag5F2A) {
        this.tag5F2A = tag5F2A;
    }

    public String getTag9F02() {
        return this.tag9F02;
    }

    public void setTag9F02(String tag9F02) {
        this.tag9F02 = tag9F02;
    }

    public String getTag9F4E() {
        return this.tag9F4E;
    }

    public void setTag9F4E(String tag9F4E) {
        this.tag9F4E = tag9F4E;
    }

    public String getTag9F36() {
        return this.tag9F36;
    }

    public void setTag9F36(String tag9F36) {
        this.tag9F36 = tag9F36;
    }

    public Date getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "EMVTransferLog{" +
                "tag9A=" + tag9A +
                ", tag9F21='" + tag9F21 + "'" +
                ", tag5F2A='" + tag5F2A + "'" +
                ", tag9F02='" + tag9F02 + "'" +
                ", tag9F4E='" + tag9F4E + "'" +
                ", tag9F36='" + tag9F36 + "'" +
                ", timeStamp='" + timeStamp + "'" +
                '}';
    }

}
