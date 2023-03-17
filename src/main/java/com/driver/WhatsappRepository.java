package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    //usermap
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }



    //22
    public Group createGroup(List<User> users){
        //  If there are only 2 users, the group is a personal chat
        Group newGroup;
        if(users.size()==2){
            newGroup = new Group(users.get(1).getName(), users.size());
        }else{
            newGroup = new Group("Group"+Integer.toString(++customGroupCount), users.size());
        }
        groupUserMap.put(newGroup, users);
        adminMap.put(newGroup, users.get(0));
        return  newGroup;
    }
    public int createMessage(String content){
        Message message=new Message(messageId++,content,new Date());
        return messageId;
    }
    public int sendMessage(Message message,User sender, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        for(User user: groupUserMap.get(group)){
            if(user.getMobile() == sender.getMobile()){
                if(groupMessageMap.containsKey(group)){
                    groupMessageMap.get(group).add(message);
                }else{
                    List<Message> temp = new ArrayList<>();
                    temp.add(message);
                    groupMessageMap.put(group, temp);
                }
                return message.getId();

            }
        }
        throw new Exception("sender is not a member of the group");
    }
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(adminMap.get(group) != approver){
            throw new Exception("the approver is not the current admin of the group");
        }
        if(!groupUserMap.get(group).contains(user)){
            throw new Exception("User is not a participant");
        }
        adminMap.replace(group,user);
        return "SUCCESS";
    }
    public int removeUser(User user) throws Exception{
        for(Group gp: groupUserMap.keySet()) {
            List<User> userList = groupUserMap.get(gp);
            if (userList.contains(user)) {
                for (User admin : adminMap.values()) {
                    if (admin == user) {
                        throw new Exception("Cannot remove admin");
                    }
                }
                groupUserMap.get(gp).remove(user);

                for (Message m: senderMap.keySet()){
                    User u = senderMap.get(m);
                    if(u == user) {
                        senderMap.remove(m);
                        groupMessageMap.get(gp).remove(m);
                        return groupUserMap.get(gp).size() + groupMessageMap.get(gp).size() + senderMap.size();
                    }
                }
            }
        }
        throw new Exception("User not found");
    }
    public String findMessage(Date start, Date end, int K) throws Exception{
        TreeMap<Integer,String> map = new TreeMap<>();
        ArrayList <Integer> list = new ArrayList<>();
        for (Message m: senderMap.keySet()){
            if( m.getTimestamp().compareTo(start) > 0 && m.getTimestamp().compareTo(end) < 0){
                map.put(m.getId(),m.getContent());
                list.add(m.getId());
            }
        }
        if (map.size() < K){
            throw new Exception("K is greater than the number of messages");
        }
        Collections.sort(list);
        int k = list.get(list.size()-K);
        return map.get(k);
    }


    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        return "SUCCESS";
    }
}