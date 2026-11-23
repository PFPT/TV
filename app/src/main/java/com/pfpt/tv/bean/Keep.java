package com.pfpt.tv.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.pfpt.tv.App;
import com.pfpt.tv.api.config.VodConfig;
import com.pfpt.tv.db.AppDatabase;
import com.pfpt.tv.impl.Diffable;
import com.pfpt.tv.utils.Task;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
public class Keep implements Diffable<Keep> {

    @NonNull
    @PrimaryKey
    @SerializedName("key")
    private String key;
    @SerializedName("siteName")
    private String siteName;
    @SerializedName("vodName")
    private String vodName;
    @SerializedName("vodPic")
    private String vodPic;
    @SerializedName("createTime")
    private long createTime;
    @SerializedName("type")
    private int type;
    @SerializedName("cid")
    private int cid;

    public static List<Keep> arrayFrom(String str) {
        Type listType = TypeToken.getParameterized(List.class, Keep.class).getType();
        List<Keep> items = App.gson().fromJson(str, listType);
        return items == null ? Collections.emptyList() : items;
    }

    public static Keep find(String key) {
        return find(VodConfig.getCid(), key);
    }

    public static Keep find(int cid, String key) {
        return AppDatabase.get().getKeepDao().find(cid, key);
    }

    public static Keep createVod(String key, int cid, String vodName, String vodPic, String siteName) {
        Keep keep = new Keep();
        keep.setKey(key);
        keep.setCid(cid);
        keep.setVodName(vodName);
        keep.setVodPic(vodPic);
        keep.setSiteName(siteName);
        keep.setCreateTime(System.currentTimeMillis());
        keep.save();
        return keep;
    }

    public static void updateNamePic(String key, String vodName, String vodPic) {
        Keep keep = find(key);
        if (keep == null) return;
        keep.setVodName(vodName);
        keep.setVodPic(vodPic);
        keep.save();
    }

    public static boolean exist(String key) {
        return AppDatabase.get().getKeepDao().find(key) != null;
    }

    public static void deleteAll() {
        Task.execute(() -> AppDatabase.get().getKeepDao().delete());
    }

    public static void delete(int cid) {
        Task.execute(() -> AppDatabase.get().getKeepDao().delete(cid));
    }

    public static void delete(String key) {
        Task.execute(() -> AppDatabase.get().getKeepDao().delete(key));
    }

    public static List<Keep> getVod() {
        return AppDatabase.get().getKeepDao().getVod();
    }

    public static List<Keep> getLive() {
        return AppDatabase.get().getKeepDao().getLive();
    }

    public static void sync(List<Config> configs, List<Keep> targets) {
        targets.forEach(target -> configs.stream()
                .filter(config -> target.getCid() == config.getId()).findFirst()
                .ifPresent(config -> target.save(Config.find(config).getId())));
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getVodName() {
        return vodName;
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public String getVodPic() {
        return vodPic;
    }

    public void setVodPic(String vodPic) {
        this.vodPic = vodPic;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getSiteKey() {
        return getKey().split(AppDatabase.SYMBOL)[0];
    }

    public String getVodId() {
        return getKey().split(AppDatabase.SYMBOL)[1];
    }

    public void save(int cid) {
        setCid(cid);
        save();
    }

    public void save() {
        Task.execute(() -> AppDatabase.get().getKeepDao().insertOrUpdate(this));
    }

    public Keep delete() {
        int cid = getCid();
        String key = getKey();
        Task.execute(() -> AppDatabase.get().getKeepDao().delete(cid, key));
        return this;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Keep it)) return false;
        return Objects.equals(getKey(), it.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    @Override
    public boolean isSameItem(Keep other) {
        return equals(other);
    }

    @Override
    public boolean isSameContent(Keep other) {
        return getVodName().equals(other.getVodName()) && getVodPic().equals(other.getVodPic()) && getCreateTime() == other.getCreateTime();
    }
}
