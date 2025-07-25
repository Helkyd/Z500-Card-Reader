package ao.metagest.z500cardreader.db.entiry;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

@Entity
public class BlackList {
    @Id(autoincrement = true)
    private Long _id;
    @Property
    private String  tag5A;//对应卡片PAN
    @Property
    private String  tag5F34;//对应卡片SN
    @Generated(hash = 575258713)
    public BlackList(Long _id, String tag5A, String tag5F34) {
        this._id = _id;
        this.tag5A = tag5A;
        this.tag5F34 = tag5F34;
    }
    @Generated(hash = 1200343381)
    public BlackList() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
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
}
