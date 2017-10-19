package teamdoppelganger.smarterbus.item;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchItem implements Parcelable {

    public String name;
    public String address;
    public double positionX;
    public double positionY;

    public SearchItem() {

    }

    public SearchItem(Parcel in) {
        name = in.readString();
        address = in.readString();
        positionX = in.readDouble();
        positionY = in.readDouble();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeString(name);
        dest.writeDouble(positionX);
        dest.writeDouble(positionY);

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public SearchItem createFromParcel(Parcel in) {
            return new SearchItem(in);
        }

        public SearchItem[] newArray(int size) {
            return new SearchItem[size];
        }

    };
}

