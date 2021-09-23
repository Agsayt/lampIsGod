package gr483.beklemishev.lampispower;

public class NetworkSettings {
    public int id;
    public String Title;
    public String Address;
    public int Port;

    public String toString()
    {
        return String.valueOf(id) + " " + Title + " " + Address + ":" + Port;
    }
}
