-- Skema database Jelajah Lombok (MariaDB 10.11 / MySQL 8)
-- Database: lombokapps

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    username VARCHAR(60) NOT NULL,
    email VARCHAR(190) NOT NULL,
    phone VARCHAR(30) NOT NULL DEFAULT '',
    city VARCHAR(120) NOT NULL DEFAULT '',
    bio TEXT NOT NULL,
    avatar_uri TEXT NOT NULL,
    role ENUM('user', 'admin') NOT NULL DEFAULT 'user',
    password_hash CHAR(64) NOT NULL,
    password_salt VARCHAR(64) NOT NULL,
    last_login_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS password_resets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    code_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_password_resets_user (user_id),
    CONSTRAINT fk_password_resets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS media_library (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(190) NOT NULL,
    mime VARCHAR(80) NOT NULL,
    size_bytes INT UNSIGNED NOT NULL,
    width INT UNSIGNED NOT NULL DEFAULT 0,
    height INT UNSIGNED NOT NULL DEFAULT 0,
    thumbnail MEDIUMBLOB NOT NULL,
    file_data LONGBLOB NOT NULL,
    created_by BIGINT UNSIGNED NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_media_created_by (created_by),
    CONSTRAINT fk_media_created_by FOREIGN KEY (created_by) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS destinations (
    id VARCHAR(64) NOT NULL,
    name VARCHAR(160) NOT NULL,
    location VARCHAR(190) NOT NULL,
    category VARCHAR(60) NOT NULL,
    rating DECIMAL(2,1) NOT NULL DEFAULT 0.0,
    review_count VARCHAR(30) NOT NULL DEFAULT '0',
    price VARCHAR(60) NOT NULL DEFAULT '',
    duration VARCHAR(60) NOT NULL DEFAULT '',
    open_hours VARCHAR(80) NOT NULL DEFAULT '',
    image_key VARCHAR(80) NOT NULL DEFAULT '',
    image_media_id BIGINT UNSIGNED NULL,
    summary TEXT NOT NULL,
    description TEXT NOT NULL,
    highlights TEXT NOT NULL,
    facilities TEXT NOT NULL,
    best_time VARCHAR(255) NOT NULL DEFAULT '',
    map_query VARCHAR(255) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_dest_image_media (image_media_id),
    CONSTRAINT fk_dest_image_media FOREIGN KEY (image_media_id)
        REFERENCES media_library (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS culinary (
    id VARCHAR(64) NOT NULL,
    name VARCHAR(160) NOT NULL,
    category VARCHAR(60) NOT NULL,
    origin VARCHAR(190) NOT NULL,
    price VARCHAR(60) NOT NULL DEFAULT '',
    rating DECIMAL(2,1) NOT NULL DEFAULT 0.0,
    review_count VARCHAR(30) NOT NULL DEFAULT '0',
    image_key VARCHAR(80) NOT NULL DEFAULT '',
    image_media_id BIGINT UNSIGNED NULL,
    description TEXT NOT NULL,
    flavor_tags TEXT NOT NULL,
    best_at VARCHAR(255) NOT NULL DEFAULT '',
    tips TEXT NOT NULL,
    related_destination_id VARCHAR(64) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_culinary_destination (related_destination_id),
    KEY idx_culinary_image_media (image_media_id),
    CONSTRAINT fk_culinary_destination FOREIGN KEY (related_destination_id)
        REFERENCES destinations (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_culinary_image_media FOREIGN KEY (image_media_id)
        REFERENCES media_library (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lodging (
    id VARCHAR(64) NOT NULL,
    title VARCHAR(160) NOT NULL,
    subtitle VARCHAR(190) NOT NULL,
    category VARCHAR(60) NOT NULL DEFAULT '',
    location VARCHAR(190) NOT NULL DEFAULT '',
    price VARCHAR(60) NOT NULL DEFAULT '',
    rating DECIMAL(2,1) NOT NULL DEFAULT 0.0,
    review_count VARCHAR(30) NOT NULL DEFAULT '0',
    duration VARCHAR(60) NOT NULL DEFAULT '',
    open_hours VARCHAR(80) NOT NULL DEFAULT '',
    image_key VARCHAR(80) NOT NULL DEFAULT '',
    image_media_id BIGINT UNSIGNED NULL,
    description TEXT NOT NULL,
    highlights TEXT NOT NULL,
    facilities TEXT NOT NULL,
    best_time VARCHAR(255) NOT NULL DEFAULT '',
    map_query VARCHAR(255) NOT NULL DEFAULT '',
    badge VARCHAR(60) NOT NULL DEFAULT '',
    icon_key VARCHAR(80) NOT NULL DEFAULT 'ic_menu_stay',
    tint_key VARCHAR(60) NOT NULL DEFAULT 'lavender',
    container_key VARCHAR(60) NOT NULL DEFAULT 'lavender_lembut',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_lodging_image_media (image_media_id),
    CONSTRAINT fk_lodging_image_media FOREIGN KEY (image_media_id)
        REFERENCES media_library (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activities (
    id VARCHAR(64) NOT NULL,
    title VARCHAR(160) NOT NULL,
    subtitle VARCHAR(190) NOT NULL,
    category VARCHAR(60) NOT NULL DEFAULT '',
    location VARCHAR(190) NOT NULL DEFAULT '',
    price VARCHAR(60) NOT NULL DEFAULT '',
    rating DECIMAL(2,1) NOT NULL DEFAULT 0.0,
    review_count VARCHAR(30) NOT NULL DEFAULT '0',
    duration VARCHAR(60) NOT NULL DEFAULT '',
    open_hours VARCHAR(80) NOT NULL DEFAULT '',
    image_key VARCHAR(80) NOT NULL DEFAULT '',
    image_media_id BIGINT UNSIGNED NULL,
    description TEXT NOT NULL,
    highlights TEXT NOT NULL,
    facilities TEXT NOT NULL,
    best_time VARCHAR(255) NOT NULL DEFAULT '',
    map_query VARCHAR(255) NOT NULL DEFAULT '',
    badge VARCHAR(60) NOT NULL DEFAULT '',
    icon_key VARCHAR(80) NOT NULL DEFAULT 'ic_menu_activity',
    tint_key VARCHAR(60) NOT NULL DEFAULT 'hijau_lombok',
    container_key VARCHAR(60) NOT NULL DEFAULT 'hijau_lombok_muda',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_activities_image_media (image_media_id),
    CONSTRAINT fk_activities_image_media FOREIGN KEY (image_media_id)
        REFERENCES media_library (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cultural_events (
    id VARCHAR(64) NOT NULL,
    title VARCHAR(160) NOT NULL,
    subtitle VARCHAR(190) NOT NULL,
    category VARCHAR(60) NOT NULL DEFAULT '',
    location VARCHAR(190) NOT NULL DEFAULT '',
    price VARCHAR(60) NOT NULL DEFAULT '',
    rating DECIMAL(2,1) NOT NULL DEFAULT 0.0,
    review_count VARCHAR(30) NOT NULL DEFAULT '0',
    duration VARCHAR(60) NOT NULL DEFAULT '',
    open_hours VARCHAR(80) NOT NULL DEFAULT '',
    image_key VARCHAR(80) NOT NULL DEFAULT '',
    image_media_id BIGINT UNSIGNED NULL,
    description TEXT NOT NULL,
    highlights TEXT NOT NULL,
    facilities TEXT NOT NULL,
    best_time VARCHAR(255) NOT NULL DEFAULT '',
    map_query VARCHAR(255) NOT NULL DEFAULT '',
    badge VARCHAR(60) NOT NULL DEFAULT '',
    icon_key VARCHAR(80) NOT NULL DEFAULT 'ic_menu_event',
    tint_key VARCHAR(60) NOT NULL DEFAULT 'coklat_tua',
    container_key VARCHAR(60) NOT NULL DEFAULT 'pasir_kuning',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_events_image_media (image_media_id),
    CONSTRAINT fk_events_image_media FOREIGN KEY (image_media_id)
        REFERENCES media_library (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trips (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(160) NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    notes TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_trips_user (user_id),
    CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trip_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    trip_id BIGINT UNSIGNED NOT NULL,
    day_number INT NOT NULL DEFAULT 1,
    time_label VARCHAR(40) NOT NULL DEFAULT '',
    title VARCHAR(160) NOT NULL,
    notes TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_trip_items_trip (trip_id),
    CONSTRAINT fk_trip_items_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS saved_destinations (
    user_id BIGINT UNSIGNED NOT NULL,
    destination_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, destination_id),
    KEY idx_saved_destination (destination_id),
    CONSTRAINT fk_saved_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_saved_destination FOREIGN KEY (destination_id)
        REFERENCES destinations (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS saved_culinary (
    user_id BIGINT UNSIGNED NOT NULL,
    culinary_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, culinary_id),
    KEY idx_saved_culinary_item (culinary_id),
    CONSTRAINT fk_saved_culinary_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_saved_culinary_item FOREIGN KEY (culinary_id)
        REFERENCES culinary (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS saved_lodging (
    user_id BIGINT UNSIGNED NOT NULL,
    lodging_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, lodging_id),
    KEY idx_saved_lodging_item (lodging_id),
    CONSTRAINT fk_saved_lodging_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_saved_lodging_item FOREIGN KEY (lodging_id)
        REFERENCES lodging (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS saved_activities (
    user_id BIGINT UNSIGNED NOT NULL,
    activity_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, activity_id),
    KEY idx_saved_activity_item (activity_id),
    CONSTRAINT fk_saved_activity_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_saved_activity_item FOREIGN KEY (activity_id)
        REFERENCES activities (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS saved_events (
    user_id BIGINT UNSIGNED NOT NULL,
    event_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, event_id),
    KEY idx_saved_event_item (event_id),
    CONSTRAINT fk_saved_event_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_saved_event_item FOREIGN KEY (event_id)
        REFERENCES cultural_events (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(64) NOT NULL,
    title VARCHAR(190) NOT NULL,
    message TEXT NOT NULL,
    time_label VARCHAR(60) NOT NULL DEFAULT '',
    icon_key VARCHAR(80) NOT NULL DEFAULT 'ic_notifications',
    tint_key VARCHAR(60) NOT NULL DEFAULT 'hijau_lombok',
    container_key VARCHAR(60) NOT NULL DEFAULT 'hijau_lombok_muda',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_reads (
    user_id BIGINT UNSIGNED NOT NULL,
    notification_id VARCHAR(64) NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, notification_id),
    KEY idx_notification_reads_notification (notification_id),
    CONSTRAINT fk_notification_reads_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_notification_reads_notification FOREIGN KEY (notification_id)
        REFERENCES notifications (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
