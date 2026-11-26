package com.pfpt.tv.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;
import androidx.viewpager.widget.ViewPager;

import com.pfpt.tv.R;
import com.pfpt.tv.api.config.VodConfig;
import com.pfpt.tv.bean.Class;
import com.pfpt.tv.bean.Config;
import com.pfpt.tv.bean.Filter;
import com.pfpt.tv.bean.Result;
import com.pfpt.tv.bean.Site;
import com.pfpt.tv.bean.Value;
import com.pfpt.tv.databinding.FragmentVodBinding;
import com.pfpt.tv.event.CastEvent;
import com.pfpt.tv.event.ConfigEvent;
import com.pfpt.tv.event.RefreshEvent;
import com.pfpt.tv.event.StateEvent;
import com.pfpt.tv.impl.Callback;
import com.pfpt.tv.impl.ConfigListener;
import com.pfpt.tv.impl.FilterListener;
import com.pfpt.tv.impl.SiteListener;
import com.pfpt.tv.model.SiteViewModel;
import com.pfpt.tv.ui.activity.HistoryActivity;
import com.pfpt.tv.ui.activity.KeepActivity;
import com.pfpt.tv.ui.activity.SearchActivity;
import com.pfpt.tv.ui.activity.VideoActivity;
import com.pfpt.tv.ui.adapter.TypeAdapter;
import com.pfpt.tv.ui.base.BaseFragment;
import com.pfpt.tv.ui.vod.VodReceiveSession;
import com.pfpt.tv.ui.vod.compose.HomeFilterEvent;
import com.pfpt.tv.ui.vod.compose.HomeFilterGroup;
import com.pfpt.tv.ui.vod.compose.HomeLinkEvent;
import com.pfpt.tv.ui.vod.compose.HomeLinkIds;
import com.pfpt.tv.ui.vod.compose.HomeSheetController;
import com.pfpt.tv.ui.vod.compose.HomeSheetKind;
import com.pfpt.tv.ui.vod.compose.HomeSheetsKt;
import com.pfpt.tv.ui.vod.compose.HomeSiteEvent;
import com.pfpt.tv.ui.vod.compose.HomeSiteIds;
import com.pfpt.tv.ui.vod.compose.HomeSiteInput;
import com.pfpt.tv.ui.vod.compose.HomeSiteRow;
import com.pfpt.tv.ui.vod.compose.VodReceiveState;
import com.pfpt.tv.utils.FileChooser;
import com.pfpt.tv.utils.ImgUtil;
import com.pfpt.tv.utils.Notify;
import com.pfpt.tv.utils.ResUtil;
import com.pfpt.tv.utils.Sniffer;
import com.pfpt.tv.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class VodFragment extends BaseFragment implements ConfigListener, SiteListener, FilterListener, TypeAdapter.OnClickListener {

    private FragmentVodBinding mBinding;
    private SiteViewModel mViewModel;
    private TypeAdapter mAdapter;
    private Result mResult;
    private final HomeSheetController homeSheets = new HomeSheetController();
    private final VodReceiveSession.Host homeReceiveHost = new VodReceiveSession.Host() {
        @Override
        public void show(VodReceiveState state) {
            homeSheets.showReceive(state);
        }

        @Override
        public void update(VodReceiveState state) {
            homeSheets.updateReceive(state);
        }

        @Override
        public boolean isOpen() {
            return homeSheets.getKind() == HomeSheetKind.Receive;
        }

        @Override
        public void dismiss() {
            if (homeSheets.getKind() == HomeSheetKind.Receive) homeSheets.dismiss();
        }
    };
    private VodReceiveSession homeReceive;
    private List<Config> homeHistoryItems = List.of();
    private final ActivityResultLauncher<Intent> linkFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
        VideoActivity.file(requireActivity(), FileChooser.getPathFromUri(result.getData().getData()));
        homeSheets.dismiss();
    });

    public static VodFragment newInstance() {
        return new VodFragment();
    }

    private FolderFragment getFragment() {
        return (FolderFragment) mBinding.pager.getAdapter().instantiateItem(mBinding.pager, mBinding.pager.getCurrentItem());
    }

    private Site getHome() {
        return VodConfig.get().getHome();
    }

    private Config getConfig() {
        return VodConfig.get().getConfig();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (homeReceive == null) homeReceive = new VodReceiveSession((Activity) context, homeReceiveHost);
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentVodBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        mBinding.title.setSelected(true);
        setRecyclerView();
        setViewModel();
        HomeSheetsKt.setHomeSheetHost(mBinding.sheetCompose, homeSheets, this::onHomeSite, this::onHomeFilter, this::onHomeHistory, this::onHomeLink, homeReceive::play, homeReceive::dismiss);
        showProgress();
        setTitle();
        setLogo();
    }

    @Override
    protected void initEvent() {
        mBinding.top.setOnClickListener(this::onTop);
        mBinding.logo.setOnClickListener(this::onLogo);
        mBinding.link.setOnClickListener(this::onLink);
        mBinding.title.setOnClickListener(this::onSite);
        mBinding.filter.setOnClickListener(this::onFilter);
        mBinding.filter.setOnLongClickListener(this::onLink);
        mBinding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        mBinding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float factor = Math.abs(verticalOffset * 1f / appBarLayout.getTotalScrollRange());
            int padding = (int) (ResUtil.dp2px(12) * factor);
            if (mBinding.type.getPaddingTop() == padding) return;
            mBinding.type.setPadding(mBinding.type.getPaddingStart(), padding, mBinding.type.getPaddingEnd(), mBinding.type.getPaddingBottom());
        });
        mBinding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mBinding.type.smoothScrollToPosition(position);
                mAdapter.setSelected(position);
                setFabVisible(position);
            }
        });
    }

    private void setRecyclerView() {
        mBinding.type.setHasFixedSize(true);
        mBinding.type.setItemAnimator(null);
        mBinding.type.setAdapter(mAdapter = new TypeAdapter(this));
        mBinding.pager.setAdapter(new PageAdapter(getChildFragmentManager()));
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        mViewModel.getResult().observe(getViewLifecycleOwner(), this::setAdapter);
    }

    private void setAdapter(Result result) {
        mAdapter.addAll(mResult = result);
        mBinding.pager.getAdapter().notifyDataSetChanged();
        setFabVisible(0);
        hideProgress();
        showContent();
    }

    private void setFabVisible(int position) {
        if (mAdapter.getItemCount() == 0) {
            mBinding.top.setVisibility(View.INVISIBLE);
            mBinding.link.setVisibility(View.VISIBLE);
            mBinding.filter.setVisibility(View.GONE);
        } else if (!mAdapter.get(position).getFilters().isEmpty()) {
            mBinding.top.setVisibility(View.INVISIBLE);
            mBinding.link.setVisibility(View.GONE);
            mBinding.filter.show();
        } else if (position == 0 || mAdapter.get(position).getFilters().isEmpty()) {
            mBinding.top.setVisibility(View.INVISIBLE);
            mBinding.filter.setVisibility(View.GONE);
            mBinding.link.show();
        }
    }

    private void setTitle() {
        List<String> items = Arrays.asList(getHome().getName(), getConfig().getName(), getString(R.string.app_name));
        Optional<String> optional = items.stream().filter(s -> !TextUtils.isEmpty(s)).findFirst();
        optional.ifPresent(s -> mBinding.title.setText(s));
    }

    private void onTop(View view) {
        getFragment().scrollToTop();
        mBinding.top.setVisibility(View.INVISIBLE);
        if (mBinding.filter.getVisibility() == View.INVISIBLE) mBinding.filter.show();
        else if (mBinding.link.getVisibility() == View.INVISIBLE) mBinding.link.show();
    }

    private boolean onLink(View view) {
        CharSequence clip = Util.getClipText();
        String text = clip == null || TextUtils.isEmpty(clip) ? "" : Sniffer.getUrl(clip.toString());
        bringHomeSheet();
        homeSheets.showLink(text == null ? "" : text);
        return true;
    }

    private void onLogo(View view) {
        List<Config> items = Config.getAll(0);
        if (items == null || items.isEmpty()) return;
        List<String> names = new ArrayList<>();
        for (Config item : items) names.add(item.getDesc() == null ? "" : item.getDesc());
        List<String> shown = HomeSheetsKt.homeHistoryNames(names, true);
        if (shown.isEmpty()) return;
        homeHistoryItems = items;
        bringHomeSheet();
        homeSheets.showHistory(shown);
    }

    private void onSite(View view) {
        List<HomeSiteRow> rows = readHomeSites();
        if (rows.isEmpty()) return;
        bringHomeSheet();
        homeSheets.showSite(rows);
    }

    private void onFilter(View view) {
        if (mAdapter.getItemCount() == 0) return;
        List<Filter> filters = mAdapter.get(mBinding.pager.getCurrentItem()).getFilters();
        if (filters == null || filters.isEmpty()) return;
        bringHomeSheet();
        homeSheets.showFilter(readHomeFilters(filters));
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.keep) KeepActivity.start(requireActivity());
        else if (item.getItemId() == R.id.search) SearchActivity.start(requireActivity());
        else if (item.getItemId() == R.id.history) HistoryActivity.start(requireActivity());
        return true;
    }

    private void showProgress() {
        mBinding.progress.getRoot().setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mBinding.progress.getRoot().setVisibility(View.GONE);
    }

    private void hideContent() {
        mBinding.type.setVisibility(View.INVISIBLE);
        mBinding.pager.setVisibility(View.INVISIBLE);
    }

    private void showContent() {
        mBinding.type.setVisibility(View.VISIBLE);
        mBinding.pager.setVisibility(View.VISIBLE);
    }

    private void homeContent() {
        showProgress();
        setFabVisible(0);
        mAdapter.clear();
        mViewModel.homeContent();
        mBinding.pager.setAdapter(new PageAdapter(getChildFragmentManager()));
    }

    public Result getResult() {
        return mResult == null ? new Result() : mResult;
    }

    private void setLogo() {
        ImgUtil.logo(mBinding.logo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigEvent(ConfigEvent event) {
        if (event.type() == ConfigEvent.Type.VOD) setLogo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        switch (event.getType()) {
            case HOME:
                setTitle();
            case SIZE:
                homeContent();
                break;
            case CATEGORY:
                getFragment().onRefresh();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStateEvent(StateEvent event) {
        switch (event.type()) {
            case EMPTY:
                hideProgress();
                break;
            case PROGRESS:
                showProgress();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCastEvent(CastEvent event) {
        if (homeSheets.getKind() == HomeSheetKind.Receive) return;
        if (event == null || event.history() == null || homeReceive == null) return;
        bringHomeSheet();
        homeReceive.show(event);
    }

    private void bringHomeSheet() {
        mBinding.sheetCompose.bringToFront();
        mBinding.sheetCompose.setVisibility(View.VISIBLE);
    }

    private List<HomeSiteRow> readHomeSites() {
        List<HomeSiteInput> inputs = new ArrayList<>();
        List<Site> sites = VodConfig.get().getSites();
        if (sites == null) return List.of();
        for (Site site : sites) {
            if (site == null) continue;
            inputs.add(new HomeSiteInput(site.getName() == null ? "" : site.getName(), site.isHide(), site.isSelected(), site.isChangeable()));
        }
        return HomeSheetsKt.homeSiteRows(inputs);
    }

    private List<Site> visibleHomeSites() {
        List<Site> items = new ArrayList<>();
        List<Site> sites = VodConfig.get().getSites();
        if (sites == null) return items;
        for (Site site : sites) if (site != null && !site.isHide()) items.add(site);
        return items;
    }

    private void onHomeSite(HomeSiteEvent event) {
        List<Site> items = visibleHomeSites();
        int index = event.getIndex();
        if (index < 0 || index >= items.size()) return;
        if (event.getId() == HomeSiteIds.OPEN) {
            setSite(items.get(index));
            return;
        }
        if (event.getId() == HomeSiteIds.CHANGE) {
            Site item = items.get(index);
            item.setChangeable(!item.isChangeable()).save();
            homeSheets.updateSite(readHomeSites());
            return;
        }
        if (event.getId() != HomeSiteIds.CHANGE_ALL) return;
        boolean next = !items.get(index).isChangeable();
        for (Site site : items) site.setChangeable(next).save();
        homeSheets.updateSite(readHomeSites());
    }

    private List<HomeFilterGroup> readHomeFilters(List<Filter> filters) {
        List<HomeFilterGroup> groups = new ArrayList<>();
        for (Filter filter : filters) {
            List<String> labels = new ArrayList<>();
            List<Boolean> selected = new ArrayList<>();
            for (Value value : filter.getValue()) {
                labels.add(value.getN() == null ? "" : value.getN());
                selected.add(value.isSelected());
            }
            groups.add(new HomeFilterGroup(labels, HomeSheetsKt.firstSelected(selected)));
        }
        return groups;
    }

    private void onHomeFilter(HomeFilterEvent event) {
        if (mAdapter.getItemCount() == 0) return;
        List<Filter> filters = mAdapter.get(mBinding.pager.getCurrentItem()).getFilters();
        int group = event.getGroup();
        if (filters == null || group < 0 || group >= filters.size()) return;
        List<Value> values = filters.get(group).getValue();
        int index = event.getIndex();
        if (index < 0 || index >= values.size()) return;
        Value value = values.get(index);
        for (Value item : values) item.setSelected(value);
        setFilter(filters.get(group).getKey(), value);
        homeSheets.updateFilter(readHomeFilters(filters));
    }

    private void onHomeHistory(int index) {
        if (index < 0 || index >= homeHistoryItems.size()) return;
        setConfig(homeHistoryItems.get(index));
    }

    private void onHomeLink(HomeLinkEvent event) {
        if (event.getId() == HomeLinkIds.CHOOSE) {
            FileChooser.from(linkFileLauncher).show();
            return;
        }
        if (event.getId() != HomeLinkIds.PLAY) return;
        String text = event.getText() == null ? "" : event.getText().trim();
        if (HomeSheetsKt.homeLinkReady(text)) VideoActivity.start(requireActivity(), text);
        homeSheets.dismiss();
    }

    @Override
    public void setConfig(Config config) {
        VodConfig.load(config, new Callback() {
            @Override
            public void start() {
                showProgress();
                hideContent();
                setTitle();
                setLogo();
            }

            @Override
            public void error(String msg) {
                Notify.dismiss();
                Notify.show(msg);
                showContent();
            }
        });
    }

    @Override
    public void setSite(Site item) {
        VodConfig.get().setHome(item);
    }

    @Override
    public void onItemClick(int position, Class item) {
        mBinding.pager.setCurrentItem(position);
        mAdapter.setSelected(position);
    }

    @Override
    public void setFilter(String key, Value value) {
        getFragment().setFilter(key, value);
    }

    @Override
    public boolean canBack() {
        if (mBinding.pager.getAdapter() == null || mBinding.pager.getAdapter().getCount() == 0) return true;
        if (!getFragment().canBack()) return true;
        getFragment().goBack();
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Class type = mAdapter.get(position);
            return FolderFragment.newInstance(getHome().getKey(), type, 4);
        }

        @Override
        public int getCount() {
            return mAdapter.getItemCount();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        }
    }
}
