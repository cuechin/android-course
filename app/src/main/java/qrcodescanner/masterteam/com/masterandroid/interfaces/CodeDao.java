package qrcodescanner.masterteam.com.masterandroid.interfaces;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Flowable;
import qrcodescanner.masterteam.com.masterandroid.utils.constant.TableNames;
import qrcodescanner.masterteam.com.masterandroid.models.Code;
import qrcodescanner.masterteam.com.masterandroid.utils.util.database.BaseDao;

@Dao

public interface CodeDao extends BaseDao<Code> {
    @Query("SELECT * FROM " + TableNames.CODES)
    Flowable<List<Code>> getAllFlowableCodes();

}
