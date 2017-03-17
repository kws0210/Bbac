package world.picpic.www.bbac.fragments;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import world.picpic.www.bbac.HomeActivity;
import world.picpic.www.bbac.R;
import world.picpic.www.bbac.common.BaseActivity;
import world.picpic.www.bbac.common.ResultCd;
import world.picpic.www.bbac.common.Url;
import world.picpic.www.bbac.util.CommonUtil;
import world.picpic.www.bbac.util.NetworkThreadTask;

public class MsgListFragment extends Fragment implements NetworkThreadTask.OnCompleteListener {
    private Activity context;
    private View fragmentView;
    private LinearLayout llAllCheck;
    private Button btnSetting, btnDelete, btnHome;
    private TextView tvAllCheck;
    private ListAdapter listAdapter;
    private ListView msgList;
    private ArrayList<MsgList> groupList;
    private ArrayList<String> seqList;
    private final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 11;
    private final int REQ_CODE_GET_MESSAGE_LIST_BY_OFFSET = 15;
    private final int REQ_CODE_UPDATE_MESSAGE_NEW = 16;
    private final int REQ_CODE_DELETE_MESSAGE = 17;
    private final int REQ_CODE_DELETE_SEND_MESSAGE = 18;
    private final int REQ_CODE_GET_SEND_MESSAGE_LIST = 19;
    private final int REQ_CODE_GET_SEND_MESSAGE_FROM_REPLY = 20;
    private final int REQ_CODE_GET_MESSAGE_FROM_REPLY = 21;
    private ViewHolderGroup selectedViewHolder;
    private boolean isDeleteMode;
    private int offsetNum = 0;
    private int msgCount;

    private enum msgType {recieved, send}

    ;
    private msgType mType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_msg_list, container, false);
        msgList = (ListView) fragmentView.findViewById(R.id.msgList);
        tvAllCheck = (TextView) context.findViewById(R.id.tvAllCheck);
        llAllCheck = (LinearLayout) context.findViewById(R.id.llAllCheck);
        btnSetting = (Button) context.findViewById(R.id.btnMy);
        btnDelete = (Button) context.findViewById(R.id.btnDelete);
        btnHome = (Button) context.findViewById(R.id.btnHome);

        groupList = new ArrayList<MsgList>();
        seqList = new ArrayList<String>();

        context.findViewById(R.id.llSendOptions).setVisibility(View.GONE);
        btnHome.setVisibility(View.VISIBLE);
        mType = msgType.recieved;

        context.findViewById(R.id.layoutTitle).setBackgroundResource(R.color.colorYellow);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = context.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorYellow));
        }

        llAllCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupList.size() == seqList.size()) {
                    tvAllCheck.setText(context.getString(R.string.select_all));
                    for (ViewHolderGroup vh : listAdapter.getVhList()) {
                        vh.btnCheck.setBackgroundResource(R.drawable.select_nor);
                    }
                    for (MsgList item : groupList)
                        item.isChecked = false;
                    seqList.clear();
                    btnDelete.setText(getResources().getString(R.string.btn_delete_nor));
                } else {
                    tvAllCheck.setText(context.getString(R.string.deselect_all));
                    for (ViewHolderGroup vh : listAdapter.getVhList()) {
                        vh.btnCheck.setBackgroundResource(R.drawable.select_pre);
                    }
                    for (MsgList item : groupList) {
                        item.isChecked = true;
                        if (!seqList.contains(item.seq))
                            seqList.add(item.seq);
                    }
                    btnDelete.setText(getResources().getString(R.string.btn_delete_pre));
                }
            }
        });
        isDeleteMode = false;

        View.OnClickListener onSettingClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listAdapter.getCount() == 0) {
                    if (mType == msgType.recieved) {
                        ((BaseActivity) context).showSelectDialogOneOption(R.string.select_msg_list_delete, R.string.select_logout, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CommonUtil.setUserPhoneNo(context, "");
                                ((HomeActivity) context).transitFragment(new HomeFragment(), false);
                            }
                        });
                    } else if (mType == msgType.send) {
                        ((BaseActivity) context).showSelectDialogOneOption(R.string.select_msg_send_list_delete, R.string.select_logout, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CommonUtil.setUserPhoneNo(context, "");
                                ((HomeActivity) context).transitFragment(new HomeFragment(), false);
                            }
                        });
                    }
                } else {
                    if (isDeleteMode) {
                        for (ViewHolderGroup vh : listAdapter.getVhList()) {
                            vh.isOpened = false;
                            selectedViewHolder = null;
                        }
                        if (seqList.size() != 0) {
                            JSONArray seqs = new JSONArray();
                            for (String seq : seqList) {
                                seqs.put(seq);
                            }

                            String json = seqs.toString();

                            if (mType == msgType.recieved) {
                                deleteMessage(json);
                            } else if (mType == msgType.send) {
                                deleteSendMessage(json);
                            }
                        } else {
                            cancelDelete();
                        }
                    } else {
                        if (mType == msgType.recieved) {
                            ((BaseActivity) context).showSelectDialog(R.string.select_msg_list_delete, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (!isDeleteMode) {
                                        btnDelete.setVisibility(View.VISIBLE);
                                        btnSetting.setVisibility(View.GONE);
                                        llAllCheck.setVisibility(View.VISIBLE);
                                        btnHome.setVisibility(View.GONE);
                                        for (ViewHolderGroup vh : listAdapter.getVhList()) {
                                            if (vh.isOpened)
                                                closeItem(vh);
                                            vh.llCheck.setVisibility(View.VISIBLE);
                                            expandDeleteButton(vh.llCheck, 500, CommonUtil.dpToPx(context, 40));
                                        }
                                        selectedViewHolder = null;
                                        isDeleteMode = true;

                                        Bundle bundle = new Bundle();
                                        if (mType == msgType.recieved)
                                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "삭제모드 : " + getString(R.string.select_msg_list_delete));
                                        else if (mType == msgType.send)
                                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "삭제모드 : " + getString(R.string.select_msg_send_list_delete));
                                        ((BaseActivity) context).getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                    }
                                }
                            }, R.string.select_logout, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CommonUtil.setUserPhoneNo(context, "");
                                    ((HomeActivity) context).transitFragment(new HomeFragment(), false);

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getString(R.string.select_logout));
                                    ((BaseActivity) context).getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                }
                            });
                        } else if (mType == msgType.send) {
                            ((BaseActivity) context).showSelectDialog(R.string.select_msg_send_list_delete, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (!isDeleteMode) {
                                        btnDelete.setVisibility(View.VISIBLE);
                                        btnSetting.setVisibility(View.GONE);
                                        llAllCheck.setVisibility(View.VISIBLE);
                                        btnHome.setVisibility(View.GONE);
                                        for (ViewHolderGroup vh : listAdapter.getVhList()) {
                                            if (vh.isOpened)
                                                closeItem(vh);
                                            vh.llCheck.setVisibility(View.VISIBLE);
                                            expandDeleteButton(vh.llCheck, 500, CommonUtil.dpToPx(context, 40));
                                        }
                                        selectedViewHolder = null;
                                        isDeleteMode = true;

                                        Bundle bundle = new Bundle();
                                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "삭제모드 : " + getString(R.string.select_msg_list_delete));
                                        ((BaseActivity) context).getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                    }
                                }
                            }, R.string.select_logout, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CommonUtil.setUserPhoneNo(context, "");
                                    ((HomeActivity) context).transitFragment(new HomeFragment(), false);

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getString(R.string.select_logout));
                                    ((BaseActivity) context).getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                }
                            });
                        }
                    }
                }
            }
        };
        btnSetting.setOnClickListener(onSettingClickListener);
        btnDelete.setOnClickListener(onSettingClickListener);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) context).transitFragment(new HomeFragment(), false);
            }
        });

        if ("".equals(CommonUtil.getUserPhoneNo(context))) {
            ((BaseActivity) context).showDialog(R.string.alert_msg_confirm, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        ((HomeActivity) context).registerUserInfo();
                    } else {
                        ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((HomeActivity) context).transitFragment(new HomeFragment(), false);
                }
            });
        } else {
            offsetNum = 0;
            getMsgList(context, offsetNum);
        }

        if (selectedViewHolder != null) {
            selectedViewHolder.isOpened = false;
            selectedViewHolder = null;
        }

        return fragmentView;
    }

    @Override
    public void onAttach(final Context activity) {
        super.onAttach(activity);
        context = (Activity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!"".equals(CommonUtil.getUserPhoneNo(context)))
            if(offsetNum == 0)
                getMsgList(context, offsetNum);
    }

    public void getMsgList(Context context, int offset) {
        if(offset == 0) {
            groupList = new ArrayList<MsgList>();
            listAdapter = new ListAdapter(context, groupList);
            msgList.setAdapter(listAdapter);
        }

        if (mType == msgType.recieved)
            requestMsgList(context, offset);
        else
            requestSendMsgList(context, offset);
    }

    private void requestMsgList(Context context, int offset) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_MESSAGE_LIST_BY_OFFSET);
        bundle.putInt("reqCode", REQ_CODE_GET_MESSAGE_LIST_BY_OFFSET);
        bundle.putString("phoneNo", CommonUtil.getUserPhoneNo(context));
        bundle.putString("offset", String.valueOf(offset));

        NetworkThreadTask mTask = new NetworkThreadTask(context, true);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void requestSendMsgList(Context context, int offset) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_SEND_MESSAGE_LIST_BY_OFFSET);
        bundle.putInt("reqCode", REQ_CODE_GET_SEND_MESSAGE_LIST);
        bundle.putString("phoneNo", CommonUtil.getUserPhoneNo(context));
        bundle.putString("offset", String.valueOf(offset));

        NetworkThreadTask mTask = new NetworkThreadTask(context, true);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void updateMessageNew(String seq) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.UPDATE_MESSAGE_NEW);
        bundle.putInt("reqCode", REQ_CODE_UPDATE_MESSAGE_NEW);
        bundle.putString("seq", seq);

        NetworkThreadTask mTask = new NetworkThreadTask(context, false);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void deleteMessage(String seqList) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.DELETE_MESSAGE);
        bundle.putInt("reqCode", REQ_CODE_DELETE_MESSAGE);
        bundle.putString("seqList", seqList);

        NetworkThreadTask mTask = new NetworkThreadTask(context, true);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void deleteSendMessage(String seqList) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.DELETE_SEND_MESSAGE);
        bundle.putInt("reqCode", REQ_CODE_DELETE_SEND_MESSAGE);
        bundle.putString("seqList", seqList);

        NetworkThreadTask mTask = new NetworkThreadTask(context, true);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void requestSendMsgFromReply(String seq) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_SEND_MESSAGE_FROM_REPLY);
        bundle.putInt("reqCode", REQ_CODE_GET_SEND_MESSAGE_FROM_REPLY);
        bundle.putString("seq", seq);

        NetworkThreadTask mTask = new NetworkThreadTask(context, true);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void requestMsgFromReply(String seq) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_SEND_MESSAGE_FROM_REPLY);
        bundle.putInt("reqCode", REQ_CODE_GET_MESSAGE_FROM_REPLY);
        bundle.putString("seq", seq);

        NetworkThreadTask mTask = new NetworkThreadTask(context, true);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    @Override
    public void onSuccess(int requestCd, String responseText) {
        JSONObject jsonObject = null;
        JSONArray jsonArray;
        String resultCd;
        try {
            jsonObject = new JSONObject(responseText);
            resultCd = jsonObject.getString("resultCd");

            if(ResultCd.SUCCESS.equals(resultCd)) {
                if ((requestCd == REQ_CODE_GET_MESSAGE_LIST_BY_OFFSET) || (requestCd == REQ_CODE_GET_SEND_MESSAGE_LIST)) {
                    msgCount = jsonObject.getInt("msgCount");
                    jsonArray = jsonObject.getJSONArray("msgList");
                    setMsgList(jsonArray, Integer.parseInt(jsonObject.getString("offset")));
                } else if (requestCd == REQ_CODE_UPDATE_MESSAGE_NEW) {
                    selectedViewHolder.isNew = false;
                    setImageOfMsgType(selectedViewHolder);
                    MsgList selectedItem = null;
                    for (MsgList item : groupList) {
                        if (item.seq.equals(selectedViewHolder.seq)) {
                            selectedItem = item;
                            break;
                        }
                    }
                    if (selectedItem != null) {
                        selectedItem.isNew = 0;
                        listAdapter.notifyDataSetChanged();
                    }
                } else if (requestCd == REQ_CODE_DELETE_MESSAGE || requestCd == REQ_CODE_DELETE_SEND_MESSAGE) {
                    tvAllCheck.setText(context.getString(R.string.select_all));
                    btnDelete.setVisibility(View.GONE);
                    btnSetting.setVisibility(View.VISIBLE);
                    llAllCheck.setVisibility(View.GONE);
                    btnHome.setVisibility(View.VISIBLE);

                    seqList.clear();
                    offsetNum = 0;
                    getMsgList(context, offsetNum);
                    isDeleteMode = false;
                } else if (requestCd == REQ_CODE_GET_SEND_MESSAGE_FROM_REPLY) {
                    selectedViewHolder.llSendMsgFromReply.setVisibility(View.VISIBLE);
                    ((TextView)selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvSendMsgFromReply)).setText(jsonObject.getString("message"));
                    ((TextView)selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvSendMsgFromReplyTime)).setText(CommonUtil.getTimeForThisApp(context,jsonObject.getString("time")));
                    ((TextView)selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvTitleItemFirst)).setText(getString(R.string.title_send_msg_list));
                    ((TextView)selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvTitleItemSecond)).setText(getString(R.string.title_msg_list));
                } else if (requestCd == REQ_CODE_GET_MESSAGE_FROM_REPLY) {
                    selectedViewHolder.llSendMsgFromReply.setVisibility(View.VISIBLE);
                    ((TextView)selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvSendMsgFromReply)).setText(jsonObject.getString("message"));
                    ((TextView)selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvSendMsgFromReplyTime)).setText(CommonUtil.getTimeForThisApp(context,jsonObject.getString("time")));
                    ((TextView)selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvTitleItemFirst)).setText(getString(R.string.title_msg_list));
                    ((TextView)selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvTitleItemSecond)).setText(getString(R.string.title_send_msg_list));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFailure(requestCd, responseText);
        }
    }

    @Override
    public void onFailure(int requestCode, String responseText) {
        Toast.makeText(context, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    private class ListAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<MsgList> itemList = new ArrayList<MsgList>();
        private ArrayList<ViewHolderGroup> vhList = new ArrayList<ViewHolderGroup>();

        public ListAdapter(Context context, ArrayList<MsgList> groupList) {
            this.context = context;
            this.itemList = groupList;
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolderGroup vh;
            final MsgList item = groupList.get(position);

//            if (convertView == null) {
                vh = new ViewHolderGroup();

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.msg_list_item, null);

                vh.msg_list_item = (LinearLayout) convertView.findViewById(R.id.msg_list_item);
                vh.llReplyAndTime = (LinearLayout) convertView.findViewById(R.id.llReplyAndTime);
                vh.llCheck = (LinearLayout) convertView.findViewById(R.id.llCheck);
                vh.btnCheck = (ImageView) convertView.findViewById(R.id.btnCheck);
                vh.llSendMsgFromReply = (LinearLayout) convertView.findViewById(R.id.llSendMsgFromReply);
                vh.tvMsg = (TextView) convertView.findViewById(R.id.tvMsg);
                vh.msgDivder = (View) convertView.findViewById(R.id.msgDivider);
                vh.tvMsgTime = (TextView) convertView.findViewById(R.id.tvMsgTime);
                vh.ivIsReply = (ImageView) convertView.findViewById(R.id.ivIsReply);
                vh.btnReply = (Button) convertView.findViewById(R.id.btnReply);
                vh.position = position;

//                Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NanumGothic.otf");
//                vh.tvMsg.setTypeface(myTypeface);

                vh.seq = item.seq;

                final String fromWhom = item.fromWhom;
                vh.btnReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((HomeActivity) context).transitFragmentByReply(new HomeFragment(), fromWhom, item.msg, item.seq);
                    }
                });

                if (item.isNew == 1) {
                    vh.isNew = true;
                } else {
                    vh.isNew = false;
                }

                vh.isReply = item.isReply;
                setImageOfMsgType(vh);

                View.OnClickListener onItemClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isDeleteMode) {
                            selectedViewHolder = vh;
                            MsgList selectedItem = null;
                            for (MsgList item : groupList) {
                                if (item.seq.equals(selectedViewHolder.seq)) {
                                    selectedItem = item;
                                    break;
                                }
                            }
                            if (selectedItem.isChecked) {
                                selectedViewHolder.btnCheck.setBackgroundResource(R.drawable.select_nor);
                                if (selectedItem != null) {
                                    selectedItem.isChecked = false;
                                }
                                seqList.remove(selectedViewHolder.seq);
                                tvAllCheck.setText(context.getString(R.string.select_all));

                                if (seqList.size() == 0)
                                    btnDelete.setText(getResources().getString(R.string.btn_delete_nor));
                            } else {
                                selectedViewHolder.btnCheck.setBackgroundResource(R.drawable.select_pre);
                                if (selectedItem != null) {
                                    selectedItem.isChecked = true;
                                }
                                seqList.add(selectedViewHolder.seq);

                                if (seqList.size() == groupList.size())
                                    tvAllCheck.setText(context.getString(R.string.deselect_all));
                                btnDelete.setText(getResources().getString(R.string.btn_delete_pre));
                            }

                        } else {
                            if (selectedViewHolder != null && !selectedViewHolder.equals(vh)) {
                                closeItem(selectedViewHolder);
                                listAdapter.notifyDataSetChanged();
                            }
                            selectedViewHolder = vh;
                            if (selectedViewHolder.isOpened) {
                                closeItem(selectedViewHolder);
                                selectedViewHolder = null;
                            } else {
                                openItem(selectedViewHolder);

                                if (position == getCount() - 1) {
                                    msgList.smoothScrollByOffset(selectedViewHolder.msg_list_item.getHeight());
                                }
                            }
                        }
                    }
                };
                convertView.setOnClickListener(onItemClickListener);
                vh.tvMsg.setOnClickListener(onItemClickListener);
                vh.tvMsg.setText(item.msg);
                vh.tvMsgTime.setText(CommonUtil.getTimeForThisApp(context, item.msgTime));

                if (isDeleteMode) {
                    vh.llCheck.setVisibility(View.VISIBLE);
                    expandDeleteButton(vh.llCheck, 0, CommonUtil.dpToPx(context, 40));
                    if (item.isChecked) {
                        vh.btnCheck.setBackgroundResource(R.drawable.select_pre);
                    } else {
                        vh.btnCheck.setBackgroundResource(R.drawable.select_nor);
                    }
                    closeItem(vh);
                } else {
                    vh.btnCheck.setBackgroundResource(R.drawable.select_nor);

                    if (selectedViewHolder != null && selectedViewHolder.seq.equals(vh.seq)) {
                        selectedViewHolder = vh;
                        openItem(vh);
                    } else {
                        closeItem(vh);
                    }
                }

                if (vh.position == itemList.size() - 1) {
                    vh.msgDivder.setVisibility(View.GONE);
                    if(msgCount < (offsetNum * 10))
                        vh.msg_list_item.setBackgroundResource(R.drawable.rounded_bottom_white_background);
                    else {
                        offsetNum++;
                        getMsgList(context, offsetNum);
                    }
                }
                vhList.add(vh);

                convertView.setTag(vh);

//            } else {
//                vh = (ViewHolderGroup) convertView.getTag();
//            }

            return convertView;
        }

        public ArrayList<ViewHolderGroup> getVhList() {
            return vhList;
        }


    }

    private class MsgList {
        public String msg;
        public String msgTime;
        public int isNew;
        public int isReply;
        public String fromWhom;
        public String seq;
        public boolean isChecked;
    }

    private class ViewHolderGroup {
        public LinearLayout msg_list_item;
        public LinearLayout llReplyAndTime;
        public LinearLayout llCheck;
        public ImageView btnCheck;
        public View msgDivder;
        public LinearLayout llSendMsgFromReply;
        public TextView tvMsg;
        public TextView tvMsgTime;
        public ImageView ivIsReply;
        public Button btnReply;
        public int isReply;
        public boolean isNew;
        public boolean isOpened;
        public String seq;
        public int position;
    }

    private void setMsgList(JSONArray jsonArray, int offset) throws Exception {
        if(offset == 0)
            groupList.clear();

        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonItem = jsonArray.getJSONObject(i);

                MsgList itemGroup = new MsgList();

                if (mType == msgType.recieved) {
                    itemGroup.msg = jsonItem.getString("message");
                    itemGroup.msgTime = jsonItem.getString("time");
                    itemGroup.fromWhom = jsonItem.getString("from_whom");
                    itemGroup.isNew = Integer.parseInt(jsonItem.getString("is_new"));
                    itemGroup.isChecked = false;
                    itemGroup.isReply = Integer.parseInt(jsonItem.getString("is_reply"));
                    itemGroup.seq = jsonItem.getString("seq");
                } else {
                    itemGroup.msg = jsonItem.getString("message");
                    itemGroup.msgTime = jsonItem.getString("time");
                    itemGroup.isChecked = false;
                    itemGroup.isReply = Integer.parseInt(jsonItem.getString("is_reply"));
                    itemGroup.seq = jsonItem.getString("seq");
                }

                groupList.add(itemGroup);

            }
            listAdapter.notifyDataSetChanged();
        } else {
            msgList.setEmptyView(fragmentView.findViewById(R.id.emptyMsgList));
            if (mType == msgType.recieved) {
                ((TextView) fragmentView.findViewById(R.id.tvEmpty1)).setText(R.string.contents_msg_list_empty1);
                ((TextView) fragmentView.findViewById(R.id.tvEmpty2)).setText(R.string.contents_msg_list_empty2);
            } else if (mType == msgType.send) {
                ((TextView) fragmentView.findViewById(R.id.tvEmpty1)).setText(R.string.contents_msg_send_list_empty1);
                ((TextView) fragmentView.findViewById(R.id.tvEmpty2)).setText(R.string.contents_msg_send_list_empty2);
            }
        }
    }

    private void openItem(final ViewHolderGroup vh) {
        vh.tvMsg.setMaxLines(300);
        vh.tvMsg.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (vh.isNew) {
            updateMessageNew(vh.seq);
        }
        vh.tvMsg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((BaseActivity) context).showSelectDialogTwoOptions(R.string.copy, R.string.block, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setText(vh.tvMsg.getText());
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                return false;
            }
        });

        if (vh.position == 0) {
            vh.msg_list_item.setBackground(getResources().getDrawable(R.drawable.rounded_lightgray_background));

        } else {
            vh.msg_list_item.setBackground(getResources().getDrawable(R.color.colorLightGray));
        }

        if (mType == msgType.recieved) {
            vh.btnReply.setVisibility(View.VISIBLE);
            if(vh.isReply != 0){
                requestSendMsgFromReply(String.valueOf(vh.isReply));
            }
        } else {
            vh.btnReply.setVisibility(View.GONE);
            if(vh.isReply != 0){
                requestMsgFromReply(String.valueOf(vh.isReply));
            }
        }
        vh.isOpened = true;
    }

    private void closeItem(ViewHolderGroup vh) {
        vh.tvMsg.setMaxLines(1);
        vh.tvMsg.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (vh.position == 0) {
            vh.msg_list_item.setBackground(getResources().getDrawable(R.drawable.rounded_top_white_background));

        } else {
            vh.msg_list_item.setBackground(getResources().getDrawable(R.color.colorWhite));
        }
        vh.tvMsg.setOnLongClickListener(null);
        vh.llSendMsgFromReply.setVisibility(View.GONE);
        vh.btnReply.setVisibility(View.GONE);
        vh.isOpened = false;

//        Layout layout = vh.tvMsg.getLayout();
//        final TextView tvItemMsg = vh.tvMsg;
//        if (layout != null) {
//            // The TextView has already been laid out
//            // We can check whether it's ellipsized immediately
//            if (layout.getLineCount() > 1) {
//                tvItemMsg.setEllipsize(TextUtils.TruncateAt.END);
//            }
//        } else {
//            // The TextView hasn't been laid out, so we need to set an observer
//            // The observer fires once layout's done, when we can check the ellipsizing
//            ViewTreeObserver vto = tvItemMsg.getViewTreeObserver();
//            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    Layout layout = tvItemMsg.getLayout();
//                    if (layout.getLineCount() > 1) {
//                        tvItemMsg.setEllipsize(TextUtils.TruncateAt.END);
//                    }
//
//                    // Remove the now unnecessary observer
//                    // It wouldn't fire again for reused views anyways
//                    ViewTreeObserver obs = tvItemMsg.getViewTreeObserver();
//                    obs.removeGlobalOnLayoutListener(this);
//                }
//            });
//        }

    }

    public void cancelDelete() {
        tvAllCheck.setText(context.getString(R.string.select_all));
        btnDelete.setVisibility(View.GONE);
        btnSetting.setVisibility(View.VISIBLE);
        llAllCheck.setVisibility(View.GONE);
        btnHome.setVisibility(View.VISIBLE);
        for (ViewHolderGroup vh : listAdapter.getVhList()) {
//            vh.llCheck.setVisibility(View.GONE);
            collapseDeleteButton(vh.llCheck, 500, CommonUtil.dpToPx(context, 00));
            vh.llReplyAndTime.setVisibility(View.VISIBLE);
            vh.btnCheck.setBackgroundResource(R.drawable.select_nor);
        }

        for (MsgList item : groupList) {
            item.isChecked = false;
        }
        seqList.clear();
        isDeleteMode = false;
    }

    public void setMsgType() {
        ((BaseActivity) context).showSelectDialogTwoOptions(R.string.title_msg_list, R.string.title_send_msg_list, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = msgType.recieved;
                ((HomeActivity) context).tvFragmentTitle.setText(getString(R.string.title_msg_list));
                offsetNum = 0;
                getMsgList(context, offsetNum);
                if (selectedViewHolder != null) {
                    selectedViewHolder.isOpened = false;
                    selectedViewHolder = null;
                }
                if (isDeleteMode)
                    cancelDelete();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "메세지 타입 : " + getString(R.string.title_msg_list));
                ((BaseActivity) context).getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = msgType.send;
                ((HomeActivity) context).tvFragmentTitle.setText(getString(R.string.title_send_msg_list));
                offsetNum = 0;
                getMsgList(context, offsetNum);
                if (selectedViewHolder != null) {
                    selectedViewHolder.isOpened = false;
                    selectedViewHolder = null;
                }
                if (isDeleteMode)
                    cancelDelete();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "메세지 타입 : " + getString(R.string.title_send_msg_list));
                ((BaseActivity) context).getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });
    }

    private void setImageOfMsgType(ViewHolderGroup vh) {
        if (mType == msgType.recieved) {
            if (vh.isNew) {
                if (vh.isReply != 0) {
                    vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_recieve_reply));
                } else {
                    vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_recieve));
                }
            } else {
                if (vh.isReply != 0) {
                    vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_recieve_reply_checked));
                } else {
                    vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_recieve_checked));
                }
            }
        } else if (mType == msgType.send) {
//            if(vh.isNew) {
            if (vh.isReply != 0) {
                vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_send_reply));
            } else {
                vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_send));
            }
//            } else {
//                if(vh.isReply) {
//                    vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_send_reply_checked));
//                } else {
//                    vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_send_checked));
//                }
//            }
        }
    }

    public static void expandDeleteButton(final View v, int duration, int targetWidth) {
        int prevWidth = v.getWidth();

        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevWidth, targetWidth);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().width = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    public static void collapseDeleteButton(final View v, int duration, int targetWidth) {
        int prevWdith = v.getWidth();

        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevWdith, targetWidth);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().width = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }
}
