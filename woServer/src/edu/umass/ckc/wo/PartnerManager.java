package edu.umass.ckc.wo;

import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.smgr.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 2/26/15
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PartnerManager {
    private static HashMap<Integer, WaitingStudent> requesters = new HashMap<Integer, WaitingStudent>();
    private static HashMap<Integer, HashSet<Integer>> requestees_requesters = new HashMap<Integer, HashSet<Integer>>();
    private static HashMap<Integer, Integer> current_matches = new HashMap<Integer, Integer>();


    public synchronized static void addRequest(Connection conn, int id, ArrayList<String> conditions) throws SQLException{
        WaitingStudent waiter = new WaitingStudent();
        waiter.setPartners(conn, id, conditions);
        ArrayList<Integer> prospPartners = waiter.getPossiblePartners();
        requesters.put(id, waiter);
        for(Integer i : prospPartners){
            if(requestees_requesters.containsKey(i)){
                requestees_requesters.get(i).add(id);
            }
            else{
                requestees_requesters.put(i, new HashSet<Integer>());
                requestees_requesters.get(i).add(id);
            }
        }
    }

    public synchronized static Integer checkForRequestingPartner(int id){
        if(requestees_requesters.containsKey(id)){
            Integer partner = requestees_requesters.get(id).iterator().next();
            requesters.get(partner).setPartner(id);
            current_matches.put(id, partner);
            ArrayList<Integer> toRemove = requesters.get(partner).getPossiblePartners();
            for(Integer rem : toRemove){
                requestees_requesters.get(rem).remove(partner);
                if(requestees_requesters.get(rem).isEmpty()){
                    requestees_requesters.remove(rem);
                }
            }
            return partner;
        }
        else{
            return null;
        }
    }

    public synchronized static Integer getRequestedPartner(int id){
        return requesters.get(id).getPartner();
    }

    public synchronized static void removeRequest(int id){
        requesters.remove(id);
        current_matches.values().remove(id);
    }

    public synchronized static boolean requestExists(int id) {
        return requesters.containsKey(id);
    }

    public synchronized static boolean isPartner(int id){
        if(current_matches.containsKey(id)){
            return true;
        }
        return false;
    }

    public synchronized static Integer getRequestingPartner(int id) {
        if(current_matches.containsKey(id)){
            return current_matches.get(id);
        }
        return null;
    }

    public synchronized static String getPartnerName(Connection conn, int id) throws SQLException{
        User u = DbUser.getStudent(conn,id);
        String name = u.getFname();
        if (name == null || name.equals(""))
            return u.getUname();
        else return name;
    }

    //TODO update partners method

    public static boolean hasEligiblePartners(Connection conn, int id, ArrayList<String> conditions) throws SQLException{
        WaitingStudent waiter = new WaitingStudent();
        waiter.setPartners(conn, id, conditions);
        return !waiter.getPossiblePartners().isEmpty();
    }

    public static void clearOldData(int id){
        removeSelfFromLists(id);
        requesters.remove(id);
        //TODO change the next two removals to only remove if prospective partner is inactive?
        if(requestees_requesters.containsKey(id)){
            requestees_requesters.remove(id);
        }
        current_matches.remove(id);
        current_matches.values().remove(id);
    }

    public synchronized static void removeSelfFromLists(int id){
        ArrayList<Integer> toRemove = null;
        WaitingStudent requester = requesters.get(id);
        if(requester != null){
            toRemove = requester.getPossiblePartners();
        }
        if(toRemove != null){
            for(Integer rem : toRemove){
                HashSet<Integer> requestee =  requestees_requesters.get(rem);
                if(requestee != null){
                    requestee.remove(id);
                    if(requestees_requesters.get(rem).isEmpty()){
                        requestees_requesters.remove(rem);
                    }
                }
            }
        }
    }

    public synchronized static void decline(int id) {
        removeSelfFromLists(id);
        removeRequest(id);
    }


    private static class WaitingStudent{
        private ArrayList<Integer> possiblePartners;
        private Integer partner = null;

        private void setPartner(Integer partner){
            this.partner = partner;
        }

        private Integer getPartner(){
            return partner;
        }

        private ArrayList<Integer> getPossiblePartners(){
            return possiblePartners;
        }

        private void setPartners(Connection conn, int id, ArrayList<String> conditions) throws SQLException {
            if(conditions.isEmpty()){
                possiblePartners = getNeighbors(conn, id);
            }
        }

        private ArrayList<Integer> getNeighbors(Connection conn, int id) throws SQLException {
            ArrayList<Integer> partners = new ArrayList<Integer>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String q = "select leftStudId, rightStudId from flankinguser where studId=?";
                ps = conn.prepareStatement(q);
                ps.setInt(1,id);
                rs = ps.executeQuery();
                rs.next();
                Integer left = rs.getInt("leftStudId");
                Integer right = rs.getInt("rightStudId");
                //This will be a problem if a  studIds is ever 0.
                if(left != 0){
                    partners.add(left);
                }
                if(right != 0){
                    partners.add(right);
                }
            } finally {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();

            }
            return partners;
        }
    }
}
