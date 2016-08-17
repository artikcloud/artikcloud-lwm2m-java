package cloud.artik.lwm2m.enums;

public enum FirmwareUpdateState {
   IDLE(1), //  1. Idle (before downloading or after updating)
   DOWNLOADING(2), // 2. Downloading (The data sequence is on the way)
   DOWNLOADED(3), // 3. Downloaded . If writing the firmware package to Package Resource is done, or, 
                  // if the device has downloaded the firmware package from the Package URI the state changes to Downloaded.
   ;
   
   private final int stateId;

   private FirmwareUpdateState(int stateId) {
       this.stateId = stateId;
   }
   
   public Long getStateAsLong() {
       return new Long(this.stateId);
   }
}
