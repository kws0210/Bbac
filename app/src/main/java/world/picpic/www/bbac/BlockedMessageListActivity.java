package world.picpic.www.bbac;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import world.picpic.www.bbac.common.BaseActivity;
import world.picpic.www.bbac.common.ResultCd;
import world.picpic.www.bbac.common.Url;
import world.picpic.www.bbac.util.CommonUtil;
import world.picpic.www.bbac.util.NetworkThreadTask;

public class BlockedMessageListActivity extends BaseActivity implements NetworkThreadTask.OnCompleteListener {

    private final int REQ_CODE_GET_BLOCKED_MESSAGE_LIST_BY_OFFSET = 1;
    private final int REQ_CODE_GET_SEND_MESSAGE_FROM_REPLY = 2;
    private final int REQ_CODE_DELETE_BLOCK = 3;

    private ImageButton btnBack;
    private ListAdapter listAdapter;
    private ListView msgList;
    private ArrayList<MsgList> groupList;
    private ViewHolderGroup selectedViewHolder;
    private int offsetNum = 0;
    private int msgCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_blocked_message_list);
        super.onCreate(savedInstanceState);

        initUI();
    }

    private void initUI() {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.animation_from_left, R.anim.animation_to_right);
            }
        });
        msgList = (ListView) findViewById(R.id.msgList);
        groupList = new ArrayList<MsgList>();

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorMint));
        }
        offsetNum = 0;
        getBlockedMsgList(this, offsetNum);

        if (selectedViewHolder != null) {
            selectedViewHolder.isOpened = false;
            selectedViewHolder = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!"".equals(CommonUtil.getUserPhoneNo(this)))
            if (offsetNum == 0)
                getBlockedMsgList(this, offsetNum);
    }

    public void getBlockedMsgList(Context context, int offset) {
        if (offset == 0) {
            groupList = new ArrayList<MsgList>();
            listAdapter = new ListAdapter(context, groupList);
            msgList.setAdapter(listAdapter);
        }

        requestMsgList(context, offset);
    }

    private void requestMsgList(Context context, int offset) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_BLOCKED_MESSAGE_LIST_BY_OFFSET);
        bundle.putInt("reqCode", REQ_CODE_GET_BLOCKED_MESSAGE_LIST_BY_OFFSET);
        bundle.putString("phoneNo", CommonUtil.getUserPhoneNo(context));
        bundle.putString("offset", String.valueOf(offset));

        NetworkThreadTask mTask = new NetworkThreadTask(context, true);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void deleteBlock(String phoneNo, String blockNo, String seq) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.DELETE_BLOCK);
        bundle.putInt("reqCode", REQ_CODE_DELETE_BLOCK);
        bundle.putString("phoneNo", phoneNo);
        bundle.putString("blockNo", blockNo);
        bundle.putString("seq", seq);

        NetworkThreadTask mTask = new NetworkThreadTask(this, true);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void requestSendMsgFromReply(String seq) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_SEND_MESSAGE_FROM_REPLY);
        bundle.putInt("reqCode", REQ_CODE_GET_SEND_MESSAGE_FROM_REPLY);
        bundle.putString("seq", seq);

        NetworkThreadTask mTask = new NetworkThreadTask(this, true);
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

            if (ResultCd.SUCCESS.equals(resultCd)) {
                if ((requestCd == REQ_CODE_GET_BLOCKED_MESSAGE_LIST_BY_OFFSET)) {
                    msgCount = jsonObject.getInt("msgCount");
                    jsonArray = jsonObject.getJSONArray("msgList");
                    setMsgList(jsonArray, Integer.parseInt(jsonObject.getString("offset")));

                    if(Integer.parseInt(jsonObject.getString("msgCount"))==0) {
                        msgList.setEmptyView(findViewById(R.id.emptyMsgList));
                        ((TextView) findViewById(R.id.tvEmpty1)).setText(R.string.contents_blocked_msg_list_empty1);
                        ((TextView) findViewById(R.id.tvEmpty2)).setText(R.string.contents_blocked_msg_list_empty2);
                    }
                } else if (requestCd == REQ_CODE_DELETE_BLOCK) {
                    offsetNum = 0;
                    getBlockedMsgList(this, offsetNum);
                } else if (requestCd == REQ_CODE_GET_SEND_MESSAGE_FROM_REPLY) {
                    selectedViewHolder.llSendMsgFromReply.setVisibility(View.VISIBLE);
                    if(!jsonObject.getString("message").equals("null"))
                        ((TextView) selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvSendMsgFromReply)).setText(jsonObject.getString("message"));
                    if(!jsonObject.getString("time").equals("null"))
                        ((TextView) selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvSendMsgFromReplyTime)).setText(CommonUtil.getTimeForThisApp(this, jsonObject.getString("time")));
                    ((TextView) selectedViewHolder.llSendMsgFromReply.findViewById(R.id.tvTitleItemFirst)).setText(getString(R.string.title_send_msg_list));
                    ((TextView) selectedViewHolder.llHeaderItem.findViewById(R.id.tvTitleItemSecond)).setText(getString(R.string.title_msg_list));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFailure(requestCd, responseText);
        }
    }

    @Override
    public void onFailure(int requestCode, String responseText) {
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
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
            convertView = inflater.inflate(R.layout.blocked_msg_list_item, null);

            vh.msg_list_item = (LinearLayout) convertView.findViewById(R.id.msg_list_item);
            vh.llReplyAndTime = (LinearLayout) convertView.findViewById(R.id.llReplyAndTime);
            vh.llSendMsgFromReply = (LinearLayout) convertView.findViewById(R.id.llSendMsgFromReply);
            vh.tvMsg = (TextView) convertView.findViewById(R.id.tvMsg);
            vh.llHeaderItem = (LinearLayout) convertView.findViewById(R.id.tvHeaderItem);
            vh.msgDivder = (View) convertView.findViewById(R.id.msgDivider);
            vh.tvMsgTime = (TextView) convertView.findViewById(R.id.tvMsgTime);
            vh.ivIsReply = (ImageView) convertView.findViewById(R.id.ivIsReply);
            vh.btnDeleteBlock = (Button) convertView.findViewById(R.id.btnDeleteBlock);
            vh.position = position;

//                Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NanumGothic.otf");
//                vh.tvMsg.setTypeface(myTypeface);

            vh.seq = item.seq;
            vh.fromWhom = item.fromWhom;

            vh.isReply = item.isReply;
            setImageOfMsgType(vh);

            vh.btnDeleteBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteBlock(CommonUtil.getUserPhoneNo(context), vh.fromWhom, vh.seq);
                }
            });

            View.OnClickListener onItemClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
            };
            convertView.setOnClickListener(onItemClickListener);
            vh.tvMsg.setOnClickListener(onItemClickListener);
            vh.tvMsg.setText(item.msg);
            vh.tvMsgTime.setText(CommonUtil.getTimeForThisApp(context, item.msgTime));


            if (selectedViewHolder != null && selectedViewHolder.seq.equals(vh.seq)) {
                selectedViewHolder = vh;
                openItem(vh);
            } else {
                closeItem(vh);
            }


            if (vh.position == itemList.size() - 1) {
                vh.msgDivder.setVisibility(View.GONE);
                if (msgCount >= (offsetNum * 10)){
                    offsetNum++;
                    getBlockedMsgList(context, offsetNum);
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
        public int isReply;
        public String fromWhom;
        public String seq;
        public boolean isChecked;
    }

    private class ViewHolderGroup {
        public LinearLayout msg_list_item;
        public LinearLayout llReplyAndTime;
        public View msgDivder;
        public LinearLayout llSendMsgFromReply;
        public TextView tvMsg;
        public LinearLayout llHeaderItem;
        public TextView tvMsgTime;
        public ImageView ivIsReply;
        public Button btnDeleteBlock;
        public int isReply;
        public boolean isOpened;
        public String seq;
        public String fromWhom;
        public int position;
    }

    private void setMsgList(JSONArray jsonArray, int offset) throws Exception {
        if (offset == 0)
            groupList.clear();

        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonItem = jsonArray.getJSONObject(i);

                MsgList itemGroup = new MsgList();


                    itemGroup.msg = jsonItem.getString("message");
                    itemGroup.msgTime = jsonItem.getString("time");
                    itemGroup.fromWhom = jsonItem.getString("from_whom");
                    itemGroup.isChecked = false;
                    itemGroup.isReply = Integer.parseInt(jsonItem.getString("is_reply"));
                    itemGroup.seq = jsonItem.getString("seq");


                groupList.add(itemGroup);

            }
            listAdapter.notifyDataSetChanged();
        }
    }

    private void openItem(final ViewHolderGroup vh) {
        vh.tvMsg.setMaxLines(300);
        vh.tvMsg.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        vh.llHeaderItem.setVisibility(View.VISIBLE);
        vh.tvMsg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showSelectDialogTwoOptions(R.string.copy, R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
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
        vh.msg_list_item.setBackground(getResources().getDrawable(R.color.colorLightGray));

        vh.btnDeleteBlock.setVisibility(View.VISIBLE);
        if (vh.isReply != 0) {
            requestSendMsgFromReply(String.valueOf(vh.isReply));
        }

        vh.isOpened = true;
    }

    private void closeItem(ViewHolderGroup vh) {
        vh.tvMsg.setMaxLines(1);
        vh.tvMsg.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        vh.llHeaderItem.setVisibility(View.GONE);
        vh.msg_list_item.setBackground(getResources().getDrawable(R.color.colorWhite));

        vh.tvMsg.setOnLongClickListener(null);
        vh.llSendMsgFromReply.setVisibility(View.GONE);
        vh.btnDeleteBlock.setVisibility(View.GONE);
        vh.isOpened = false;

    }

    private void setImageOfMsgType(ViewHolderGroup vh) {
        if (vh.isReply != 0) {
            vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_recieve_reply));
        } else {
            vh.ivIsReply.setBackground(getResources().getDrawable(R.drawable.arrow_recieve));
        }
    }
}
