package ao.metagest.z500cardreader.utils.tlv;

/**
 * @author Administrator on 2016/9/5.
 */
class LPosition {

    private int _vL;
    private int _position;

    LPosition(int _vL, int position) {
        this._vL = _vL;
        this._position = position;
    }

    int get_vL() {
        return _vL;
    }

    void set_vL(int _vL) {
        this._vL = _vL;
    }

    int get_position() {
        return _position;
    }

    void set_position(int _position) {
        this._position = _position;
    }

}
