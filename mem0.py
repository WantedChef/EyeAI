from mem0 import MemoryClient

# Vervang 'm0-HN5W5Pts2QGFLt27emKUejivDn4dVIRrvSmVPvSh' door je eigen API-sleutel
client = MemoryClient(api_key="your-api-key")

# Voorbeeld: Maak een nieuwe memory aan
item = client.create_memory(
    title="Mijn eerste memory",
    content="Dit is een voorbeeld van data opslaan in Mem0 via Python."
)
print("Memory aangemaakt:", item)

# Voorbeeld: Haal alle memories op
memories = client.list_memories()
print("Overzicht van memories:")
for mem in memories:
    print(f"ID: {mem['id']}, Titel: {mem['title']}, Inhoud: {mem['content']}")
