import React from 'react';

const AddressManagement = ({
  addressForm,
  handleAddressChange,
  handleAddressSubmit,
  addresses,
  handleAddressEdit,
  handleAddressDelete,
  editingAddressId,
  addressMessage,
  addressErrors, // NEW: object of field errors
  submitting      // NEW: disable while submitting
}) => {
  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">
        {editingAddressId ? 'Edit Address' : 'Add Address'}
      </h2>

      <form onSubmit={handleAddressSubmit} className="space-y-4" noValidate>
        <div>
          <input
            type="text"
            name="address"
            value={addressForm.address}
            onChange={handleAddressChange}
            placeholder="Enter address"
            className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
            minLength={6}
            maxLength={50}
            required
          />
          {addressErrors.address && (
            <p className="text-red-400 text-sm mt-1">{addressErrors.address}</p>
          )}
        </div>

        <div>
          <input
            type="text"
            name="city"
            value={addressForm.city}
            onChange={handleAddressChange}
            placeholder="Enter city"
            className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
            maxLength={50}
            required
          />
          {addressErrors.city && (
            <p className="text-red-400 text-sm mt-1">{addressErrors.city}</p>
          )}
        </div>

        <div>
          <input
            type="text"
            name="state"
            value={addressForm.state}
            onChange={handleAddressChange}
            placeholder="Enter state"
            className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
            maxLength={50}
            required
          />
          {addressErrors.state && (
            <p className="text-red-400 text-sm mt-1">{addressErrors.state}</p>
          )}
        </div>

        <div>
          <input
            type="text"
            name="country"
            value={addressForm.country}
            onChange={handleAddressChange}
            placeholder="Enter country"
            className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
            maxLength={50}
            required
          />
          {addressErrors.country && (
            <p className="text-red-400 text-sm mt-1">{addressErrors.country}</p>
          )}
        </div>

        <div>
          <input
            type="text"
            name="pincode"
            value={addressForm.pincode}
            onChange={handleAddressChange}
            placeholder="Enter pincode"
            className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
            inputMode="numeric"
            pattern="^[0-9]{5,6}$"
            title="Enter a 5 or 6 digit pincode"
            required
          />
          {addressErrors.pincode && (
            <p className="text-red-400 text-sm mt-1">{addressErrors.pincode}</p>
          )}
        </div>

        <button
          type="submit"
          className="w-full bg-cyan-500 hover:bg-cyan-600 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold py-2 px-4 rounded-lg"
          disabled={submitting}
        >
          {submitting
            ? (editingAddressId ? 'Updating...' : 'Adding...')
            : (editingAddressId ? 'Update Address' : 'Add Address')}
        </button>

        {addressMessage && (
          <p className="text-center text-sm mt-2 text-yellow-400">{addressMessage}</p>
        )}
      </form>

      <hr className="my-6 border-gray-600" />

      <h2 className="text-xl font-semibold mb-4">Existing Addresses</h2>
      <ul className="space-y-2">
        {addresses.map((addr) => (
          <li key={addr.addressId} className="bg-gray-700 p-3 rounded flex justify-between items-start">
            <div>
              <p className="font-bold">{addr.address}</p>
              <p className="text-sm text-gray-300">
                {addr.city}, {addr.state}, {addr.country} - {addr.pincode}
              </p>
            </div>
            <div className="space-x-2">
              <button
                onClick={() => handleAddressEdit(addr)}
                className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded"
              >
                Edit
              </button>
              <button
                onClick={() => handleAddressDelete(addr.addressId)}
                className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded"
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default AddressManagement;
