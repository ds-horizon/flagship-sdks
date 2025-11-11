import Foundation
import CoreData

@objc(FeatureFlagEntity)
public class FeatureFlagEntity: NSManagedObject {
    
}

extension FeatureFlagEntity {
    
    @nonobjc public class func fetchRequest() -> NSFetchRequest<FeatureFlagEntity> {
        return NSFetchRequest<FeatureFlagEntity>(entityName: "FeatureFlagEntity")
    }
    
    @NSManaged public var key: String
    @NSManaged public var jsonData: Data?
    @NSManaged public var createdAt: Date
    @NSManaged public var updatedAt: Date
}
