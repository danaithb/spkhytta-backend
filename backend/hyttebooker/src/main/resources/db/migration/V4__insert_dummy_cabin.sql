INSERT INTO cabins (cabin_name, location, capacity, description)
SELECT 'Fjellhytte', 'Geilo, Norge', 6, 'En koselig hytte med utsikt over fjellene, perfekt for en avslappende helg.'
WHERE NOT EXISTS (
    SELECT 1 FROM cabins WHERE cabin_name = 'Fjellhytte'
);
