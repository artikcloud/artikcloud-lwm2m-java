package cloud.artik.lwm2m.enums;

public enum FirmwareUpdateState {
   IDLE(0),             // before downloading or after successful updating
   DOWNLOADING(1),      // Downloading (The data sequence is on the way)
   DOWNLOADED(2),       // if the device has downloaded the firmware package from the Package URI. Writing Package Resource done.
   UPDATING(3);         // When in Downloaded state, and the executable Resource Update is triggered, the state changes to Updating.
   
   private final int stateId;

   FirmwareUpdateState(int stateId) {
       this.stateId = stateId;
   }
   
   public Long getStateAsLong() {
       return new Long(this.stateId);
   }
}
