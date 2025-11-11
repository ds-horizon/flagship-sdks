import Foundation
import CoreData

public class CoreDataUtility {
    
    private let persistentContainer: NSPersistentContainer
    
    public static let shared = CoreDataUtility()
    
    private init() {
        // Create a programmatic CoreData model
        let model = NSManagedObjectModel()
        
        // Create FeatureFlagEntity
        let entity = NSEntityDescription()
        entity.name = "FeatureFlagEntity"
        entity.managedObjectClassName = "FeatureFlagEntity"
        
        // Add attributes
        let keyAttribute = NSAttributeDescription()
        keyAttribute.name = "key"
        keyAttribute.attributeType = .stringAttributeType
        keyAttribute.isOptional = false
        
        let jsonDataAttribute = NSAttributeDescription()
        jsonDataAttribute.name = "jsonData"
        jsonDataAttribute.attributeType = .binaryDataAttributeType
        jsonDataAttribute.isOptional = false
        
        let createdAtAttribute = NSAttributeDescription()
        createdAtAttribute.name = "createdAt"
        createdAtAttribute.attributeType = .dateAttributeType
        createdAtAttribute.isOptional = false
        
        let updatedAtAttribute = NSAttributeDescription()
        updatedAtAttribute.name = "updatedAt"
        updatedAtAttribute.attributeType = .dateAttributeType
        updatedAtAttribute.isOptional = false
        
        entity.properties = [keyAttribute, jsonDataAttribute, createdAtAttribute, updatedAtAttribute]
        model.entities = [entity]
        
        // Create persistent container with programmatic model
        persistentContainer = NSPersistentContainer(name: "FeatureFlagDataModel", managedObjectModel: model)
        
        // Load the persistent stores
        persistentContainer.loadPersistentStores { storeDescription, error in
            if let error = error {
                print("CoreData: Initialization failed - \(error.localizedDescription)")
            }
        }
        
        // Configure the view context
        persistentContainer.viewContext.automaticallyMergesChangesFromParent = true
    }
    
    public var context: NSManagedObjectContext {
        return persistentContainer.viewContext
    }
    
    public func saveContext() {
        let context = persistentContainer.viewContext
        
        guard context.persistentStoreCoordinator != nil else {
            print("CoreData: Persistent store coordinator is nil, skipping save")
            return
        }
        
        if context.hasChanges {
            do {
                try context.save()
            } catch {
                print("CoreData: Save error - \(error.localizedDescription)")
                context.rollback()
            }
        }
    }
    
    public func clearAllData() {
        let context = persistentContainer.viewContext
        
        guard context.persistentStoreCoordinator != nil else {
            print("CoreData: Persistent store coordinator is nil, skipping clear")
            return
        }
        
        let fetchRequest: NSFetchRequest<NSFetchRequestResult> = FeatureFlagEntity.fetchRequest()
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        
        do {
            try context.execute(deleteRequest)
            saveContext()
        } catch {
            print("CoreData: Clear error - \(error.localizedDescription)")
            context.rollback()
        }
    }
    
    public func fetchEntity(for key: String) -> FeatureFlagEntity? {
        let context = persistentContainer.viewContext
        
        guard context.persistentStoreCoordinator != nil else {
            print("CoreData: Persistent store coordinator is nil, skipping fetch for key: \(key)")
            return nil
        }
        
        let fetchRequest: NSFetchRequest<FeatureFlagEntity> = FeatureFlagEntity.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "key == %@", key)
        fetchRequest.fetchLimit = 1
        
        do {
            let results = try context.fetch(fetchRequest)
            return results.first
        } catch {
            print("CoreData: Fetch error for key '\(key)' - \(error.localizedDescription)")
            return nil
        }
    }
    
    public func createOrUpdateEntity(for key: String, jsonData: Data) -> FeatureFlagEntity? {
        let context = persistentContainer.viewContext
        
        guard context.persistentStoreCoordinator != nil else {
            print("CoreData: Persistent store coordinator is nil, skipping entity creation for key: \(key)")
            return nil
        }
        
        guard !key.isEmpty, !jsonData.isEmpty else {
            print("CoreData: Invalid key or empty data, skipping entity creation for key: \(key)")
            return nil
        }
        
        if let existingEntity = fetchEntity(for: key) {
            existingEntity.jsonData = jsonData
            existingEntity.updatedAt = Date()
            return existingEntity
        } else {
            let entity = FeatureFlagEntity(context: context)
            entity.key = key
            entity.jsonData = jsonData
            entity.createdAt = Date()
            entity.updatedAt = Date()
            return entity
        }
    }
}
