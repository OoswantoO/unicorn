package edu.tjrac.swant.todo.view;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.yckj.baselib.util.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.tjrac.swant.qqmusic.PackageSearchResult;
import edu.tjrac.swant.todo.JDApi;
import edu.tjrac.swant.todo.adapter.TodoListAdapter;
import edu.tjrac.swant.todo.bean.Todo;
import edu.tjrac.swant.unicorn.Net;
import edu.tjrac.swant.unicorn.R;
import edu.tjrac.swant.unicorn.SharedStartActivity;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ToDoMainActivity extends SharedStartActivity implements View.OnClickListener {


    //[] location () time {} link

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.recycle_main) RecyclerView mRecycleMain;
    //    @BindView(R.id.recycle_menu) RecyclerView mRecycleMenu;
//    @BindView(R.id.nav_view) NavigationView mNavView;
//    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
//    @BindView(R.id.fl_create) FrameLayout mFlCreate;
    @BindView(R.id.fab_add) FloatingActionButton fab;

    Todo target;

    ArrayList<Todo> todolist = new ArrayList<>();//树形list
    ArrayList<Todo> formatList = new ArrayList<>();//排序后

    TodoListAdapter todoAdapter;

    @Override
    protected void onResume() {
        super.onResume();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String text = "";
        try {
            if (clipboard != null && clipboard.hasText()) {
                CharSequence tmpText = clipboard.getText();
//                clipboard.setText(tmpText);
                if (tmpText != null && tmpText.length() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    text = tmpText.toString().trim();
//                    T.show(mContext,text);
                    if (StringUtils.isMobileNO(text)) {
                        builder.setTitle("添加联系人");
                    } else if (StringUtils.isEmail(text)) {
                        builder.setTitle("添加联系人（email）");
                    } else {
                        Net.getInstance().getJDApi().searchPackage(text, JDApi.key)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<PackageSearchResult>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(PackageSearchResult packageSearchResult) {
                                        PackageResultActivity.start(mContext, new Gson().toJson(packageSearchResult));
                                    }
                                });

                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            text = "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_main);
        ButterKnife.bind(this);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        mDrawerLayout.addDrawerListener(toggle);
//        toggle.syncState();

//        mFlCreate.setOnClickListener(this);

        todoAdapter = new TodoListAdapter(formatList);
        todoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                formatList.get(position).switchExpend();
                formatList.clear();
                formatList.addAll(formatList(todolist));
                todoAdapter.notifyDataSetChanged();
            }
        });
        todoAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.options_one) {


                } else if (view.getId() == R.id.options_three) {
                    target = formatList.get(position);
                    addTodo();
                } else if (view.getId() == R.id.cb_done) {
                    formatList.get(position).switchState();
                    todoAdapter.notifyItemChanged(position);
                }
            }
        });
        mRecycleMain.setLayoutManager(new LinearLayoutManager(mContext));
        mRecycleMain.setAdapter(todoAdapter);

        fab.setOnClickListener(this);
    }

    ImageView icon;
    EditText title;
    RecyclerView rv_icons;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.fl_create:
//
//
//                break;
            case R.id.fab_add:
                target = null;
                addTodo();
                break;
        }
    }

    private AlertDialog dialog;

    public void addTodo() {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mContext.getResources().getString(R.string.create_new_list));
            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_new_orderlist, null);
            icon = view.findViewById(R.id.iv_icon);
            title = view.findViewById(R.id.et_title);
            rv_icons = view.findViewById(R.id.recycle_icon);

            builder.setView(view);
            builder.setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String str = title.getText().toString().trim();
                    if (!StringUtils.isEmpty(str)) {
                        Todo todo = new Todo(str);
                        if (target == null) {
                            todolist.add(todo);
                        } else {
                            target.addChild(todo);
                        }
                        formatList.clear();
                        formatList.addAll(formatList(todolist));
                        todoAdapter.notifyDataSetChanged();
                    }
                }
            });
            dialog = builder.create();
        } else {

        }
        dialog.show();
    }


    public ArrayList<Todo> formatList(ArrayList<Todo> list) {
        ArrayList<Todo> formatList = new ArrayList<>();
        for (Todo item : list) {
            formatList.addAll(item.format());
        }
        return formatList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(mContext).inflate(R.menu.options_todos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}