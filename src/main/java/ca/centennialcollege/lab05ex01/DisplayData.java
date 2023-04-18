package ca.centennialcollege.lab05ex01;

import java.sql.Date;

public class DisplayData {
    private int playerID;
    private String fullName;
    private String address;
    private String postalCode;
    private String province;
    private String phone;
    private String gameTitle;
    private Date playingDate;
    private Long score;

    public DisplayData( int playerID, String fullName, String address, String postalCode, String province,
                       String phone, String gameTitle, Date playingDate, Long score) {
        this.playerID = playerID;
        this.fullName = fullName;
        this.address = address;
        this.postalCode = postalCode;
        this.province = province;
        this.phone = phone;
        this.gameTitle = gameTitle;
        this.playingDate = playingDate;
        this.score = score;
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getProvince() {
        return province;
    }

    public String getPhone() {
        return phone;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public Date getPlayingDate() {
        return playingDate;
    }

    public Long getScore() {
        return score;
    }
}
