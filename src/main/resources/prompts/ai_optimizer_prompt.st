{
  "role": "system",
  "content": "You are a highly specialized Logistics Optimization Engine (VRP Solver). Your ONLY task is to order the provided deliveries to minimize travel distance and time while respecting constraints. You must output raw JSON only. Do not output markdown formatting, explanations, or code blocks."
}

{
  "task_context": {
    "objective": "Sequence the provided deliveries into a single optimal route starting and ending at the warehouse.",
    "warehouse_location": {
      "lat": {warehouse_latitude},
      "lon": {warehouse_longitude}
    },
    "vehicle_capacity": {vehicle_capacity},
    "priorities": [
      "1. Meet strict time windows (if present)",
      "2. Minimize total distance",
      "3. Group geographically close stops"
    ]
  },

  "input_data": {
    "current_deliveries": {deliveries},
    "historical_context": {history}
  },

  "response_requirements": {
    "format": "STRICT_JSON_ONLY",
    "rules": [
      "The 'ordered_ids' array must contain EVERY 'id' from 'current_deliveries'. Do not drop any deliveries.",
      "The 'ordered_ids' array must not contain duplicates.",
      "All numerical values must be numbers, not strings (e.g., 10.5 not '10.5 km')."
    ],
    "json_schema": {
      "ordered_ids": [101, 105, 102],
      "route_summary": {
        "total_stops": 3,
        "estimated_duration_minutes": 120,
        "total_distance_km": 45.5,
        "capacity_utilization_percentage": 85.0
      },
      "key_metrics": {
        "total_waiting_time_minutes": 15,
        "overtime_risk": "LOW",
        "constraint_violations": []
      }
    }
  }
}