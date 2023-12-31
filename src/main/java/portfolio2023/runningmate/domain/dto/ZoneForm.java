package portfolio2023.runningmate.domain.dto;

import lombok.Data;
import portfolio2023.runningmate.domain.Zone;

@Data
public class ZoneForm {

    private String zoneName;

    public String getCityName(){return zoneName.substring(0, zoneName.indexOf("("));}

    public String getProvinceName() {
        return zoneName.substring(zoneName.indexOf("/")+1);
    }

    public String getLocalNameOfCity(){
        return zoneName.substring(zoneName.indexOf("(") + 1, zoneName.indexOf(")"));
    }

    public Zone getZone(){
        return Zone.builder()
                .city(this.getCityName())
                .localNameOfCity(this.getLocalNameOfCity())
                .province(this.getProvinceName())
                .build();
    }

}
