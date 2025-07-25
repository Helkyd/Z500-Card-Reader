package ao.metagest.z500cardreader.bean;

import java.io.Serializable;

/** 上次交易结果 */
public class LastTransResultBean implements Serializable {
    private static final long serialVersionUID = -1L;

    private String transResult;
    private String tag95;
    private String tag9B;
    private String tagDF51;

    public LastTransResultBean() {

    }

    public LastTransResultBean(String transResult, String tag95, String tag9B, String tagDF51) {
        this.transResult = transResult;
        this.tag95 = tag95;
        this.tag9B = tag9B;
        this.tagDF51 = tagDF51;
    }

    public String getTransResult() {
        return transResult;
    }

    public void setTransResult(String transResult) {
        this.transResult = transResult;
    }

    public String getTag95() {
        return tag95;
    }

    public void setTag95(String tag95) {
        this.tag95 = tag95;
    }

    public String getTag9B() {
        return tag9B;
    }

    public void setTag9B(String tag9B) {
        this.tag9B = tag9B;
    }

    public String getTagDF51() {
        return tagDF51;
    }

    public void setTagDF51(String tagDF51) {
        this.tagDF51 = tagDF51;
    }

}
