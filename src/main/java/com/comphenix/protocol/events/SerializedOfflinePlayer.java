/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.events;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

import com.comphenix.protocol.utility.ByteBuddyFactory;

/**
 * Represents a player object that can be serialized by Java.
 * 
 * @author Kristian
 */
class SerializedOfflinePlayer implements OfflinePlayer, Serializable {

	/**
	 * Generated by Eclipse.
	 */
	private static final long serialVersionUID = -2728976288470282810L;

	private transient Location bedSpawnLocation;
	
	// Relevant data about an offline player
	private String name;
	private UUID uuid;
	private long firstPlayed;
	private long lastPlayed;
	private boolean operator;
	private boolean banned;
	private boolean playedBefore;
	private boolean online;
	private boolean whitelisted;

	private static final Constructor<?> proxyPlayerConstructor = setupProxyPlayerConstructor();

	/**
	 * Constructor used by serialization.
	 */
	public SerializedOfflinePlayer() {
		// Do nothing
	}
	
	/**
	 * Initialize this serializable offline player from another player.
	 * @param offline - another player.
	 */
	public SerializedOfflinePlayer(OfflinePlayer offline) {
		this.name = offline.getName();
		this.uuid = offline.getUniqueId();
		this.firstPlayed = offline.getFirstPlayed();
		this.lastPlayed = offline.getLastPlayed();
		this.operator = offline.isOp();
		this.banned = offline.isBanned();
		this.playedBefore = offline.hasPlayedBefore();
		this.online = offline.isOnline();
		this.whitelisted = offline.isWhitelisted();
	}
	
	@Override
	public boolean isOp() {
		return operator;
	}

	@Override
	public void setOp(boolean operator) {
		this.operator = operator;
	}

	@Override
	public Map<String, Object> serialize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getBedSpawnLocation() {
		return bedSpawnLocation;
	}

	// TODO do we need to implement this?
	
	public void incrementStatistic(Statistic statistic) throws IllegalArgumentException { }

	public void decrementStatistic(Statistic statistic) throws IllegalArgumentException { }

	public void incrementStatistic(Statistic statistic, int i) throws IllegalArgumentException { }

	public void decrementStatistic(Statistic statistic, int i) throws IllegalArgumentException { }

	public void setStatistic(Statistic statistic, int i) throws IllegalArgumentException { }

	public int getStatistic(Statistic statistic) throws IllegalArgumentException {
		return 0;
	}

	public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException { }

	public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException { }

	public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		return 0;
	}

	public void incrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException { }

	public void decrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException { }

	public void setStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException { }

	public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException { }

	public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException { }

	public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		return 0;
	}

	public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException { }

	public void decrementStatistic(Statistic statistic, EntityType entityType, int i) { }

	public void setStatistic(Statistic statistic, EntityType entityType, int i) { }

	@Override
	public Location getLastDeathLocation() {
		return null;
	}

	@Override
	public long getFirstPlayed() {
		return firstPlayed;
	}

	@Override
	public long getLastPlayed() {
		return lastPlayed;
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public PlayerProfile getPlayerProfile() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean hasPlayedBefore() {
		return playedBefore;
	}

	@Override
	public boolean isBanned() {
		return banned;
	}

	public void setBanned(boolean banned) {
		this.banned = banned;
	}
	
	@Override
	public boolean isOnline() {
		return online;
	}

	@Override
	public boolean isWhitelisted() {
		return whitelisted;
	}

	@Override
	public void setWhitelisted(boolean whitelisted) {
		this.whitelisted = whitelisted;
	}

	private void writeObject(ObjectOutputStream output) throws IOException {
		output.defaultWriteObject();
		
		// Serialize the bed spawn location
		output.writeUTF(bedSpawnLocation.getWorld().getName());
		output.writeDouble(bedSpawnLocation.getX());
		output.writeDouble(bedSpawnLocation.getY());
		output.writeDouble(bedSpawnLocation.getZ());
	}
	
	private void readObject(ObjectInputStream input) throws ClassNotFoundException, IOException {
		input.defaultReadObject();

		// Well, this is a problem
		bedSpawnLocation = new Location(
				getWorld(input.readUTF()),
				input.readDouble(),
				input.readDouble(),
				input.readDouble()
		);
	}
	
	private World getWorld(String name) {
		try {
			// Try to get the world at least
			return Bukkit.getServer().getWorld(name);
		} catch (Exception e) {
			// Screw it
			return null;
		}
	}
	
	@Override
	public Player getPlayer() {
		try {
			// Try to get the real player underneath
			return Bukkit.getServer().getPlayerExact(name);
		} catch (Exception e) {
			return getProxyPlayer();
		}
	}
	
	/**
	 * Retrieve a player object that implements OfflinePlayer by referring to this object.
	 * <p>
	 * All other methods cause an exception.
	 * @return Proxy object.
	 */
	public Player getProxyPlayer() {
		try {
			return (Player) proxyPlayerConstructor.newInstance(this);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access reflection.", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot instantiate object.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error in invocation.", e);
		}
	}

	private static Constructor<? extends Player> setupProxyPlayerConstructor()
	{
		final Method[] offlinePlayerMethods = OfflinePlayer.class.getMethods();
		final String[] methodNames = new String[offlinePlayerMethods.length];
		for (int idx = 0; idx < offlinePlayerMethods.length; ++idx)
			methodNames[idx] = offlinePlayerMethods[idx].getName();

		final ElementMatcher.Junction<ByteCodeElement> forwardedMethods = ElementMatchers.namedOneOf(methodNames);

		try {
			final MethodDelegation forwarding = MethodDelegation.withDefaultConfiguration()
					.withBinders(Pipe.Binder.install(Function.class))
					.to(new Object() {
						@RuntimeType
						public Object intercept(@Pipe Function<OfflinePlayer, Object> pipe,
												@FieldValue("offlinePlayer") OfflinePlayer proxy) {
							return pipe.apply(proxy);
						}
					});

			final InvocationHandlerAdapter throwException = InvocationHandlerAdapter.of((obj, method, args) -> {
					throw new UnsupportedOperationException(
						"The method " + method.getName() + " is not supported for offline players.");
				});

			return ByteBuddyFactory.getInstance()
					.createSubclass(PlayerUnion.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
					.name(SerializedOfflinePlayer.class.getPackage().getName() + ".PlayerInvocationHandler")

					.defineField("offlinePlayer", OfflinePlayer.class, Visibility.PRIVATE)
					.defineConstructor(Visibility.PUBLIC)
					.withParameters(OfflinePlayer.class)
					.intercept(MethodCall.invoke(Object.class.getDeclaredConstructor())
							.andThen(FieldAccessor.ofField("offlinePlayer").setsArgumentAt(0)))

					.method(forwardedMethods)
					.intercept(forwarding)

					.method(ElementMatchers.not(forwardedMethods))
					.intercept(throwException)

					.make()
					.load(ByteBuddyFactory.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
					.getLoaded()
					.getDeclaredConstructor(OfflinePlayer.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Failed to find Player constructor!", e);
		}
	}

	/**
	 * This interface extends both OfflinePlayer and Player (in that order) so that the class generated by ByteBuddy
	 * looks at OfflinePlayer's methods first while still being a Player.
	 */
	private interface PlayerUnion extends OfflinePlayer, Player
	{
	}
}
