package com.example.dayPlanner.controller;

import com.example.dayPlanner.entity.Event;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Pattern;


@RestController
public class Controller {

    String url =  "jdbc:mysql://localhost:3306/events";
    String user = "root";
    String password = "123456";
    private static final Pattern datePattern = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern timePattern = Pattern.compile(
            "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]");
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
    int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    public ArrayList<Event> getAllEvents(){
        ArrayList<Event> events = new ArrayList<>();
        try{
            Connection myConn = DriverManager.getConnection(url,user,password);
            Statement myStmt = myConn.createStatement();
            ResultSet myRs = myStmt.executeQuery("select * from events");
            int i = 0;
            while(myRs.next()){
                events.add(new Event());
                events.get(i).setId(Integer.parseInt(myRs.getString("id")));
                events.get(i).setType(myRs.getString("type"));
                events.get(i).setMarker(myRs.getString("marker"));
                events.get(i).setDate(myRs.getString("date"));
                events.get(i).setTime(myRs.getString("time"));
                events.get(i).setDescription(myRs.getString("description"));
                i++;
            }
        }catch(Exception exc){
            exc.printStackTrace();
        }
        return events;
    }

    public ArrayList<Event> getEventsByDate(String minDate, String maxDate){
        ArrayList<Event> events = new ArrayList<>();
        try{
            Connection myConn = DriverManager.getConnection(url,user,password);
            Statement myStmt = myConn.createStatement();
            ResultSet myRs = myStmt.executeQuery("select * from events where date between '" + minDate +
                    "' AND '" + maxDate + "'");
            int i = 0;
            while(myRs.next()){
                events.add(new Event());
                events.get(i).setId(Integer.parseInt(myRs.getString("id")));
                events.get(i).setType(myRs.getString("type"));
                events.get(i).setMarker(myRs.getString("marker"));
                events.get(i).setDate(myRs.getString("date"));
                events.get(i).setTime(myRs.getString("time"));
                events.get(i).setDescription(myRs.getString("description"));
                i++;
            }
        }catch(Exception exc){
            exc.printStackTrace();
        }
        return events;
    }

    public boolean validate(Event event){
        int year = Integer.parseInt(event.getDate().substring(0, 4));
        int month = Integer.parseInt(event.getDate().substring(5, 7));
        int day = Integer.parseInt(event.getDate().substring(8, 10));

        YearMonth yearMonthObject = YearMonth.of(year, month);
        if(event.getMarker() == null) return false;

        if(!datePattern.matcher(event.getDate()).matches()){
            return false;
        }
        if(year == currentYear){
            if(month< currentMonth) return false;
            if(month == currentMonth){
                if(day < currentDay) return false;
            }
        }

        if((year < currentYear && month < currentMonth && day < currentDay) || year< currentYear ||
                currentMonth > 12 || currentDay > yearMonthObject.lengthOfMonth()){
            return false;
        }

        if(!timePattern.matcher(event.getTime()).matches()){
            return false;
        }
        return true;
    }
    public boolean isValidYear(String year){
        return Integer.parseInt(year) >= 0 && Integer.parseInt(year) >= currentYear;
    }

    public boolean isValidMonth(String month){
        return Integer.parseInt(month) >= 0 && Integer.parseInt(month) <= 12;
    }
    public boolean isValidDay(String day, YearMonth obj){
        return Integer.parseInt(day) <= obj.lengthOfMonth();
    }

    public String validateMonth(String month){
        String prefixMonth = "-";
        if(month.length() == 1) {
            prefixMonth = "-0";
        }
        return prefixMonth + month;
    }

    public String validateDay(String day){
        String prefixDay = "-";
        if(day.length() == 1) {
            prefixDay = "-0";
        }
        return prefixDay + day;
    }

    @RequestMapping(
            value = "/events",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayList<Event>> getEvents(){
        ArrayList<Event> events = getAllEvents();
        if(events.size() == 0){
            System.out.println("There aren' t any events!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/events/date/{year}/{month}/{day}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayList<Event>> getEventByDay(@PathVariable("year")String year,
                                                          @PathVariable("month")String month,@PathVariable("day")String day){
        ArrayList<Event> events = getAllEvents();
        ArrayList<Event> eventsByDate = new ArrayList<>();

        YearMonth obj = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
        if(!isValidYear(year) || !isValidMonth(month) || !isValidDay(day,obj)){
            System.out.println("Invalid date!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        month = validateMonth(month);
        day = validateDay(day);

        String date = year + month + day;

        for (Event event : events) {
            if (date.equals(event.getDate())) {
                eventsByDate.add(event);
            }
        }
        if(eventsByDate.size() == 0) {
            System.out.println("There aren' t any events at "+ date + "!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(eventsByDate, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/events/date/{year}/{month}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayList<Event>> getEventByMonth(@PathVariable("year")String year,
                                                          @PathVariable("month")String month){
        ArrayList<Event> events = getAllEvents();
        ArrayList<Event> eventsByDate = new ArrayList<>();

        if(!isValidYear(year) || !isValidMonth(month)){
            System.out.println("Invalid date!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        month = validateMonth(month);

        for (Event event : events) {
            String y = event.getDate().substring(0, 4);
            String m = event.getDate().substring(5, 7);
            if (y.equals(year) && m.equals(month.substring(1, 3))) {
                eventsByDate.add(event);
            }
        }

        if(eventsByDate.size() == 0) {
            System.out.println("There aren' t any events at "+ year + month + "!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(eventsByDate, HttpStatus.OK);
    }


    @RequestMapping(
            value = "/events/type/{type}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayList<Event>> getEventByType(@PathVariable("type")String type){
        ArrayList<Event> events = getAllEvents();
        ArrayList<Event> eventsByType = new ArrayList<>();

        for (Event event : events) {
            if(type.toLowerCase().equals(event.getType()) || type.toUpperCase().equals(event.getType())){
                eventsByType.add(event);
            }
        }
        if(eventsByType.size() == 0) {
            System.out.println("There aren' t any events of '"+ type +"' type!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(eventsByType, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/events/startDate/{startYear}/{startMonth}/{startDay}/endDate/{endYear}/{endMonth}/{endDay}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayList<Event>> getEventByPeriod(@PathVariable("startYear")String startYear,
                                                             @PathVariable("startMonth")String startMonth,
                                                             @PathVariable("startDay")String startDay,
                                                             @PathVariable("endYear")String endYear,
                                                             @PathVariable("endMonth")String endMonth,
                                                             @PathVariable("endDay")String endDay){
        ArrayList<Event> eventsByDate;
        YearMonth obj = YearMonth.of(Integer.parseInt(startYear), Integer.parseInt(startMonth));
        if(!isValidYear(startYear) || !isValidMonth(startMonth) || !isValidDay(startDay,obj)){
            System.out.println("Invalid date!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if(!isValidYear(endYear) || !isValidMonth(endMonth) || !isValidDay(endDay,obj)){
            System.out.println("Invalid date!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        startMonth = validateMonth(startMonth);
        startDay = validateDay(startDay);

        endMonth = validateMonth(endMonth);
        endDay = validateDay(endDay);

        String startDate = startYear + startMonth + startDay;
        String endDate = endYear + endMonth + endDay;

        eventsByDate = getEventsByDate(startDate, endDate);

        if(eventsByDate.size() == 0) {
            System.out.println("There aren' t any events in this period: "+ startDate +"/" + endDate + " !");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(eventsByDate, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/events",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Event> insertEvent(@RequestBody Event event){

        try{
            if(validate(event)) {
                Connection myConn = DriverManager.getConnection(url,user,password);
                Statement myStmt = myConn.createStatement();

                myStmt.executeUpdate("INSERT INTO events (type, marker,date,time,description)" + "VALUES ('" + event.getType() + "','"
                        + event.getMarker() + "','" + event.getDate() + "','" + event.getTime() + "','" + event.getDescription() + "')  ");
            }else{
                System.out.println("Invalid input for the event!");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
            value = "/events/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Event> updateEvent(@PathVariable int id, @RequestBody Event event) {
        try{
            if(validate(event)) {
                if(id != event.getId()){
                    System.out.println("You can't change the id of event!");
                }
                Connection myConn = DriverManager.getConnection(url, user, password);
                String query = "update events set type = ?, marker = ?, date = ?, time = ?, " +
                        "description = ? where id = ?";
                PreparedStatement myStmt = myConn.prepareStatement(query);
                myStmt.setString(1, event.getType());
                myStmt.setString(2, event.getMarker());
                myStmt.setString(3, event.getDate());
                myStmt.setString(4, event.getTime());
                myStmt.setString(5, event.getDescription());
                myStmt.setInt(6, id);

                myStmt.executeUpdate();
            }else{
                System.out.println("Invalid input for the event!");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
            value = "/events/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Event> getEvent(@PathVariable int id) {
        Event event = new Event();
        try{
            Connection myConn = DriverManager.getConnection(url,user,password);
            Statement myStmt = myConn.createStatement();
            ResultSet myRs = myStmt.executeQuery("select * from events where id = " + id + ";");
            while(myRs.next()){
                event.setId(Integer.parseInt(myRs.getString("id")));
                event.setType(myRs.getString("type"));
                event.setMarker(myRs.getString("marker"));
                event.setDate(myRs.getString("date"));
                event.setTime(myRs.getString("time"));
                event.setDescription(myRs.getString("description"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        if(event.getId() == 0){
            System.out.println("There isn' t any event with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(event,HttpStatus.OK);
    }

    @RequestMapping(
            value = "/events/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteEvent(@PathVariable int id) {
        try{
            Connection myConn = DriverManager.getConnection(url,user,password);
            String query = "delete from events where id = ? ";
            PreparedStatement myStmt = myConn.prepareStatement(query);
            myStmt.setInt(1, id);

            myStmt.executeUpdate();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}