package world.picpic.www.bbac.common;

/**
 * Created by Wonseob on 2016. 8. 16..
 */
public class Url {
    public static final String DOMAIN 				= "http://58.229.105.157/apis/bbak/";
    public static final String GET_VERSION_INFO     = DOMAIN + "get_version_info.php";
    public static final String REGISTER             = DOMAIN + "register.php";
    public static final String GET_MESSAGE_COUNT    = DOMAIN + "get_message_count.php";
    public static final String GET_GOOGLE_PLAY_ADDRESS  = DOMAIN + "get_google_play_address.php";
    public static final String SEND_MESSAGE         = DOMAIN + "send_message.php";
    public static final String SEND_SMS             = DOMAIN + "send_sms.php";
    public static final String REPLY_MESSAGE        = DOMAIN + "reply_message.php";
    public static final String REPLY_SMS            = DOMAIN + "reply_sms.php";
    public static final String GET_MESSAGE_LIST     = DOMAIN + "get_message_list.php";
    public static final String GET_MESSAGE_LIST_BY_OFFSET       = DOMAIN + "get_message_list_by_offset.php";
    public static final String GET_SEND_MESSAGE_LIST            = DOMAIN + "get_send_message_list.php";
    public static final String GET_SEND_MESSAGE_LIST_BY_OFFSET  = DOMAIN + "get_send_message_list_by_offset.php";
    public static final String GET_SEND_MESSAGE_FROM_REPLY      = DOMAIN + "get_send_message_from_reply.php";
    public static final String GET_BLOCKED_MESSAGE_LIST_BY_OFFSET = DOMAIN + "get_blocked_message_list_by_offset.php";
    public static final String UPDATE_MESSAGE_NEW   = DOMAIN + "update_message_new.php";
    public static final String DELETE_MESSAGE       = DOMAIN + "delete_message.php";
    public static final String DELETE_SEND_MESSAGE  = DOMAIN + "delete_send_message.php";
    public static final String BLOCK_USER           = DOMAIN + "block_user.php";
    public static final String DELETE_BLOCK         = DOMAIN + "delete_block.php";
    public static final String GET_RECEIVE_SMS      = DOMAIN + "get_receive_sms.php";
    public static final String UPDATE_RECEIVE_SMS      = DOMAIN + "update_receive_sms.php";
}
