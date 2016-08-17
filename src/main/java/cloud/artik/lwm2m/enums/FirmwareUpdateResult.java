package cloud.artik.lwm2m.enums;

/**
 * Java enum representing the result of a Firmware download/update.
 *  
 * @author maneesh.sahu
 */
public enum FirmwareUpdateResult {
   DEFAULT(0), //  0: Default value. Once the updating process is initiated, this Resource SHOULD be reset to default value.
   SUCCESS(1), // 1: Firmware updated successfully,
   NO_STORAGE(2), // 2: Not enough storage for the new firmware package.
   OUT_OF_MEMORY(3), // 3. Out of memory during downloading process.
   CONNECTION_LOST(4), // 4: Connection lost during downloading process.
   CRC_CHECK_FAILURE(5), // 5: CRC check failure for new downloaded package.
   UNSUPPORTED_PACKAGE_TYPE(6), // 6: Unsupported package type.
   INVALID_URI(7), // 7: Invalid URI
   FAILED(8) // 8: Failed
   ;
   
   private final int resultId;

   private FirmwareUpdateResult(int resultId) {
       this.resultId = resultId;
   }
   
   public Long getResultAsLong() {
       return new Long(this.resultId);
   }
}
